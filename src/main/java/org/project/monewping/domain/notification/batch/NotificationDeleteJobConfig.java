package org.project.monewping.domain.notification.batch;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NotificationDeleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationRepository notificationRepository;
    private final NotificationJobExecutionListener jobExecutionListener;
    private final DataSource dataSource;

    @Bean(name = "배치 작업 정의")
    public Job deleteOldNotificationsJob() {
        return new JobBuilder("deleteOldNotificationsJob", jobRepository)
            .start(deleteOldNotificationsStep())
            .listener(jobExecutionListener)
            .build();
    }

    @Bean(name = "하나의 단위 작업 정의 (알림 조회 -> 삭제)")
    public Step deleteOldNotificationsStep() {
        return new StepBuilder("deleteOldNotificationsStep", jobRepository)
            .<UUID, UUID>chunk(100, transactionManager)
            .reader(notificationReader())
            .writer(notificationWriter())
            .faultTolerant()
            .retry(TransientDataAccessException.class)
            .retryLimit(3)
            .skip(DataIntegrityViolationException.class)
            .skipLimit(10)
            .noSkip(RuntimeException.class)
            .build();
    }

    @Bean(name = "삭제할 알림 조회")
    public ItemReader<UUID> notificationReader() {
        Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        return new JdbcCursorItemReaderBuilder<UUID>()
            .name("notificationCursorReader")
            .dataSource(dataSource)
            .sql("""
                SELECT id
                FROM notifications
                WHERE confirmed = true
                  AND updated_at < ?
            """)
            .preparedStatementSetter(ps -> ps.setObject(1, oneWeekAgo))
            .rowMapper((rs, rowNum) ->
                rs.getObject("id", UUID.class)
            )
            .build();
    }

    @Bean
    public ItemWriter<UUID> notificationWriter() {
        return items -> {
            List<UUID> ids = new ArrayList<>(items.getItems());

            notificationRepository.deleteByIdIn(ids);

            log.info("알림 {}건 삭제 완료", ids.size());
        };
    }
}