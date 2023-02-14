package it.pagopa.gov.rtdmsexporter.configuration;

import it.pagopa.gov.rtdmsexporter.batch.ExportJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

@EnableScheduling
@Configuration
@Slf4j
public class ExportJobScheduleConfiguration {

  private final ExportJob exportJob;

  public ExportJobScheduleConfiguration(ExportJob exportJob) {
    this.exportJob = exportJob;
  }

  @Scheduled(cron = "${batch.cron_expression}")
  public void scheduledJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    final var start = new Date();
    log.info("Export job started at {}", start);
    final var execution = exportJob.execute();
    log.info("Export job ends at {}", execution.getEndTime());
  }
}