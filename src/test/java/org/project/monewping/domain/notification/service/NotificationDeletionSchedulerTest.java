package org.project.monewping.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.notification.batch.NotificationDeletionScheduler;
import org.project.monewping.domain.notification.exception.NotificationBatchRunException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDeletionScheduler 단위 테스트")
public class NotificationDeletionSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job deleteOldNotificationsJob;

    @InjectMocks
    private NotificationDeletionScheduler scheduler;

    @Test
    @DisplayName("runJob 호출 시 JobLauncher.run이 호출되어야 한다")
    void runJob_success() throws Exception {
        // given
        JobExecution execution = org.mockito.Mockito.mock(JobExecution.class);
        given(jobLauncher.run(eq(deleteOldNotificationsJob), any(JobParameters.class)))
            .willReturn(execution);

        // when
        scheduler.runJob();

        // then
        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(deleteOldNotificationsJob), paramsCaptor.capture());

        JobParameters captured = paramsCaptor.getValue();

        assertThat(captured.getParameters()).containsKey("date");
        assertThat(captured.getString("date")).isEqualTo(LocalDate.now().toString());
    }

    @Test
    @DisplayName("runJob 중 예외 발생 시 NotificationBatchRunException을 던진다")
    void runJob_failure() throws Exception {
        // given:
        given(jobLauncher.run(any(Job.class), any(JobParameters.class)))
            .willThrow(new RuntimeException("DB 에러"));

        // when & then
        assertThatThrownBy(() -> scheduler.runJob())
            .isInstanceOf(NotificationBatchRunException.class)
            .hasMessageContaining("알림 삭제 배치 실행 실패")
            .hasRootCauseMessage("DB 에러");
    }
}
