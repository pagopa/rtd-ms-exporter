package it.pagopa.gov.rtdmsexporter.infrastructure.step;

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
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.pagopa.gov.rtdmsexporter.configuration.ExportJobModule.ACQUIRER_GENERATED_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@Import(MockMongoConfiguration.class)
@ContextConfiguration(classes = { ExportJobModule.class, AppConfiguration.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@TestPropertySource(locations = "classpath:application.yml")
class ExportToFileStepTest {

  @Autowired
  private ExportToFileStep exportToFileStep;

  @Autowired
  private MongoTemplate mongoTemplate;

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
            .collect(Collectors.toList());
    when(mongoTemplate.find(any(Query.class), eq(CardEntity.class), anyString())).thenReturn(cards);

    // Run the job and check the result
    assertThat(exportToFileStep.execute()).isNotEmpty();

    final var written = Files.readAllLines(Path.of(ACQUIRER_GENERATED_FILE));
    assertThat(written).hasSameElementsAs(
            cards.stream()
                    .flatMap(it -> Stream.concat(Stream.of(it.getHashPan()), it.getHashPanChildren().stream()))
                    .collect(Collectors.toList())
    );
  }
}