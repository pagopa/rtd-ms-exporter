package it.pagopa.gov.rtdmsexporter.ports;

import it.pagopa.gov.rtdmsexporter.batch.ExportJobService;
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
public class JobSchedulerConfig {

  private final ExportJobService exportJobService;

  public JobSchedulerConfig(ExportJobService exportJobService) {
    this.exportJobService = exportJobService;
  }

  @Scheduled(cron = "${exporter.cronExpression}")
  public void scheduledJob() throws Exception {
    final var start = new Date();
    log.info("Export job started at {}", start);
    final var execution = exportJobService.execute();
    log.info("Export job ends at {}", execution.getEndTime());
  }
}
