package it.pagopa.gov.rtdmsexporter.ports;

import it.pagopa.gov.rtdmsexporter.application.ExportJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@Configuration
@Slf4j
public class JobSchedulerConfig {

  private final ExportJobService exportJobService;

  public JobSchedulerConfig(ExportJobService exportJobService) {
    this.exportJobService = exportJobService;
  }

  @Scheduled(cron = "${exporter.cronExpression}")
  public void scheduledJob() {
    exportJobService.execute();
  }
}
