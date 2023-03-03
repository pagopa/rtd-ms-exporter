package it.pagopa.gov.rtdmsexporter.application;

import com.mongodb.MongoException;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.pagopa.gov.rtdmsexporter.application.acquirer.SaveAcquirerFileStep;
import it.pagopa.gov.rtdmsexporter.application.paymentinstrument.NewExportedSubscriber;
import it.pagopa.gov.rtdmsexporter.configuration.AcquirerModule;
import it.pagopa.gov.rtdmsexporter.configuration.DatabaseMockConfiguration;
import it.pagopa.gov.rtdmsexporter.configuration.DatabaseReaderModule;
import it.pagopa.gov.rtdmsexporter.domain.acquirer.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import it.pagopa.gov.rtdmsexporter.utils.HashStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.pagopa.gov.rtdmsexporter.configuration.AcquirerModule.ACQUIRER_GENERATED_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = { PagedDatabaseExportStepTest.MockDeps.class, AcquirerModule.class, DatabaseReaderModule.class })
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@TestPropertySource(locations = "classpath:application.yml")
class PagedDatabaseExportStepTest {

  @Autowired
  private PagedDatabaseExportStep pagedDatabaseExportStep;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private NewExportedSubscriber newExportedSubscriber;

  @BeforeEach
  void setup() {
    when(newExportedSubscriber.apply(any())).thenReturn(CompletableFuture.completedFuture(0L));
  }

  @AfterEach
  public void cleanUp() throws IOException {
    Files.deleteIfExists(Path.of(ACQUIRER_GENERATED_FILE));
    reset(mongoTemplate);
  }

  @Test
  void whenCardsAvailableThenWriteToFileFlatten() throws Exception {
    final var cards = HashStream.of(20)
            .map(it -> new CardEntity(it, HashStream.of(2, it).collect(Collectors.toList()), "", false))
            .collect(Collectors.toList());
    when(mongoTemplate.find(any(Query.class), eq(CardEntity.class), anyString())).thenReturn(cards);

    // Run the job and check the result
    assertThat(pagedDatabaseExportStep.execute()).isTrue();

    final var written = Files.readAllLines(Path.of(ACQUIRER_GENERATED_FILE));
    final var expectedEntries = cards.stream()
            .flatMap(it -> Stream.concat(Stream.of(it.getHashPan()), it.getHashPanChildren().stream()))
            .toList();

    assertThat(written).isNotEmpty().hasSameElementsAs(expectedEntries);
  }

  @Test
  void whenMongoThrowsExceptionThenAnExceptionIsThrown() {
    when(mongoTemplate.find(any(Query.class), eq(CardEntity.class), anyString())).thenThrow(new MongoException("Unexpected error"));
    assertThrows(RuntimeException.class, () -> pagedDatabaseExportStep.execute());
  }

  @TestConfiguration
  @Import(DatabaseMockConfiguration.class)
  static class MockDeps {
    @MockBean
    SaveAcquirerFileStep saveAcquirerFileStep;
    @MockBean
    AcquirerFileRepository acquirerFileRepository;
    @MockBean
    NewExportedSubscriber newExportedSubscriber;

    @Bean
    Scheduler rxScheduler() {
      return Schedulers.io();
    }
  }
}