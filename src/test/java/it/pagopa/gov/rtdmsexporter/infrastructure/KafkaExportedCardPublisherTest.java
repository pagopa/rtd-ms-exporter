package it.pagopa.gov.rtdmsexporter.infrastructure;

import it.pagopa.gov.rtdmsexporter.application.paymentinstrument.NewExportedSubscriber;
import it.pagopa.gov.rtdmsexporter.configuration.NewExportedModule;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ActiveProfiles("integration")
@SpringBootTest(classes = { NewExportedModule.class, KafkaExportedCardPublisherTest.MockDeps.class })
@EnableAutoConfiguration
@Testcontainers
@ExtendWith(SpringExtension.class)
class KafkaExportedCardPublisherTest {

  @Container
  public static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))
          .withEmbeddedZookeeper();

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
    registry.add("test.broker", kafkaContainer::getBootstrapServers);
    registry.add("test.partitionCount", () -> 1);
  }

  @Autowired
  private ExportedCardPublisher exportedCardPublisher;

  @BeforeEach
  void setup() {
    if (!kafkaContainer.isRunning()) {
      kafkaContainer.start();
    }
  }

  @Test
  void whenNotifyExportedThenPublishValidCloudEvent() {
    final var expectedEvent = "{\"type\":\"confirmExport\",\"data\":{\"paymentInstrumentId\":\"123\"}}";
    assertThat(exportedCardPublisher.notifyExportedCard("123")).contains("123");

    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      final var record = KafkaTestUtils.getOneRecord(
              kafkaContainer.getBootstrapServers(),
              "group", "rtd-split-by-pi", 0,
              true, true, Duration.ofSeconds(5));
      assertThat(record.value().toString()).isEqualTo(expectedEvent);
    });
  }

  @Test
  void whenPublishFailThenReturnFailure() {
    kafkaContainer.stop();
    assertThat(exportedCardPublisher.notifyExportedCard("123")).isEmpty();
  }

  @TestConfiguration
  static class MockDeps {
    @MockBean
    NewExportedSubscriber newExportedSubscriber;
  }
}