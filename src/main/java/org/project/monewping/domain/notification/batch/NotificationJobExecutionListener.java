package org.project.monewping.domain.notification.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("== 알림 삭제 배치 작업 시작 ==");
        log.info("JobId: {}", jobExecution.getJobId());
        log.info("시작 시간: {}", jobExecution.getStartTime());
        log.info("JobParameters: {}", jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("== 알림 삭제 배치 작업 종료 ==");
        log.info("종료 상태: {}", jobExecution.getStatus());
        log.info("종료 시간: {}", jobExecution.getEndTime());

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            jobExecution.getStepExecutions().forEach(stepExecution -> {
                log.info("Step 이름: {}", stepExecution.getStepName());
                log.info("읽은 알림 수: {}", stepExecution.getReadCount());
                log.info("삭제된 알림 수: {}", stepExecution.getWriteCount());
                log.info("처리 실패 건수: {}", stepExecution.getSkipCount());
            });
        } else {
            log.error("배치 실패 - Status: {}, ExitCode: {}",
                jobExecution.getStatus(), jobExecution.getExitStatus().getExitCode());
        }
    }
}