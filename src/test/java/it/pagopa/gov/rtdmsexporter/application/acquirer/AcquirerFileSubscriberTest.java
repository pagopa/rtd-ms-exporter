package it.pagopa.gov.rtdmsexporter.application.acquirer;

import io.reactivex.rxjava3.core.Flowable;
import it.pagopa.gov.rtdmsexporter.configuration.AppConfiguration;
import it.pagopa.gov.rtdmsexporter.configuration.ExportJobModule;
import it.pagopa.gov.rtdmsexporter.configuration.MockMongoConfiguration;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import it.pagopa.gov.rtdmsexporter.utils.HashStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.pagopa.gov.rtdmsexporter.configuration.ExportJobModule.ACQUIRER_GENERATED_FILE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {ExportJobModule.class, MockMongoConfiguration.class, AppConfiguration.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@TestPropertySource(locations = "classpath:application.yml")
class AcquirerFileSubscriberTest {

  @Autowired
  private AcquirerFileSubscriber acquirerFileSubscriber;

  @BeforeEach
  void setup() {
  }

  @AfterEach
  public void cleanUp() throws IOException {
    Files.deleteIfExists(Path.of(ACQUIRER_GENERATED_FILE));
  }

  @Test
  void whenCardsAvailableThenWriteToFileFlatten() throws Exception {
    final var cards = HashStream.of(20)
            .map(it -> new CardEntity(it, HashStream.of(2, it).collect(Collectors.toList()), "", false))
            .toList();

    assertThat(acquirerFileSubscriber.apply(Flowable.just(cards))).succeedsWithin(Duration.ofSeconds(5));

    final var written = Files.readAllLines(Path.of(ACQUIRER_GENERATED_FILE));
    final var expectedEntries = cards.stream()
            .flatMap(it -> Stream.concat(Stream.of(it.getHashPan()), it.getHashPanChildren().stream()))
            .toList();

    assertThat(written).isNotEmpty().hasSameElementsAs(expectedEntries);
  }

  @Test
  void whenSourceContainsErrorThenSubscriberFails() throws IOException {
    assertThat(acquirerFileSubscriber.apply(Flowable.error(new Exception()))).failsWithin(Duration.ZERO);
    assertThat(Files.readAllLines(Path.of(ACQUIRER_GENERATED_FILE))).isEmpty();
  }

}