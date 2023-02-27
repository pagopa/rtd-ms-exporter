package it.pagopa.gov.rtdmsexporter.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@TestConfiguration
public class SyncJobExecutor {
  @Bean
  TaskExecutor taskExecutor() {
    return new SyncTaskExecutor();
  }
}
