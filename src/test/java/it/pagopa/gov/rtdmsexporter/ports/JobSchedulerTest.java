package it.pagopa.gov.rtdmsexporter.ports;

import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.application.ExportJobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;

import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JobSchedulerConfig.class, JobSchedulerTest.Config.class })
@TestPropertySource(properties = "exporter.cronExpression=0/5 * * * * *")
class JobSchedulerTest {

  @Autowired
  private ExportJobService exportJobService;

  @Test
  void shouldRunJobRepeatedly() throws Exception {
    when(exportJobService.execute()).thenReturn(Try.success(true));
    await().atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> verify(exportJobService, atLeast(2)).execute());
  }

  static class Config {
    @Bean
    ExportJobService exportJobService() {
      return mock(ExportJobService.class);
    }
  }
}