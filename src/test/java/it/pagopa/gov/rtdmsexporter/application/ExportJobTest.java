package it.pagopa.gov.rtdmsexporter.application;

import com.mongodb.MongoException;
import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.configuration.*;
import it.pagopa.gov.rtdmsexporter.domain.acquirer.AcquirerFile;
import it.pagopa.gov.rtdmsexporter.domain.acquirer.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardPublisher;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.ZipUtils;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import it.pagopa.gov.rtdmsexporter.utils.HashStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.function.StreamBridge;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = { ExportJobTest.Config.class, DatabaseReaderModule.class, AcquirerModule.class, NewExportedModule.class, AppConfiguration.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@TestPropertySource(locations = "classpath:application.yml")
class ExportJobTest {

  private static final String TEST_ACQUIRER_FILE = "output.csv";
  private static final String TEST_ZIP_ACQUIRER_FILE = "output.zip";

  @Autowired
  private ExportJobService exportJobService;

  // downstream deps
  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private AcquirerFileRepository acquirerFileRepository;

  @Autowired
  private ExportedCardPublisher exportedCardPublisher;

  @Autowired
  private ExportedCardRepository exportedCardRepository;

  @BeforeEach
  void setup() {
    final var cards = HashStream.of(20)
            .map(it -> new CardEntity(it, HashStream.of(2, it).collect(Collectors.toList()), "", false))
            .collect(Collectors.toList());
    when(mongoTemplate.find(any(Query.class), eq(CardEntity.class), anyString())).thenReturn(cards);
    when(exportedCardPublisher.notifyExportedCard(any())).thenAnswer(
            AdditionalAnswers.returnsElementsOf(cards.stream().map(CardEntity::getHashPan).map(Try::success).toList())
    );
  }

  @AfterEach
  void cleanup() throws IOException {
    Files.deleteIfExists(Path.of(TEST_ACQUIRER_FILE));
    Files.deleteIfExists(Path.of(TEST_ZIP_ACQUIRER_FILE));
    reset(mongoTemplate, acquirerFileRepository);
  }

  @Test
  void whenCardAvailableThenCompleteJob() throws Exception {
    final var captor = ArgumentCaptor.forClass(AcquirerFile.class);
    when(acquirerFileRepository.save(any())).thenReturn(true);
    assertThat(exportJobService.execute()).contains(true);
    verify(acquirerFileRepository, times(1)).save(captor.capture());
    verify(exportedCardPublisher, times(20)).notifyExportedCard(any());
    assertThat(captor.getValue().file().getPath()).contains(".zip");
    assertThat(exportedCardRepository.exportedPaymentInstruments()).isEmpty();
  }

  @Test
  void whenExportToFileFailThenSkipUploadAndJobFail() throws Exception {
    when(mongoTemplate.find(any(Query.class), eq(CardEntity.class), anyString())).thenThrow(new MongoException("Error"));
    assertThat(exportJobService.execute()).isEmpty();
    verify(acquirerFileRepository, times(0)).save(any());
    verify(exportedCardPublisher, times(0)).notifyExportedCard(any());
  }

  @Test
  void whenZipFailThenSkipUploadAndFail() throws Exception {
    try (final var zipUtils = mockStatic(ZipUtils.class)) {
      zipUtils.when(() -> ZipUtils.zipFile(any(), any())).thenReturn(Optional.empty());
      assertThat(exportJobService.execute()).contains(false);
      verify(acquirerFileRepository, times(0)).save(any());
    }
  }

  @Test
  void whenUploadFailThenSkipPublish() throws Exception {
    when(acquirerFileRepository.save(any())).thenReturn(false);
    assertThat(exportJobService.execute()).contains(false);
    verify(exportedCardPublisher, times(0)).notifyExportedCard(any());
    assertThat(exportedCardRepository.exportedPaymentInstruments()).isNotEmpty();
  }

  @Test
  void whenUploadFailThenJobFail() throws Exception {
    when(acquirerFileRepository.save(any())).thenReturn(false);
    assertThat(exportJobService.execute()).contains(false);
  }

  @TestConfiguration
  @Import(DatabaseMockConfiguration.class)
  static class Config {
    @MockBean
    AcquirerFileRepository acquirerFileRepository;

    @MockBean
    ExportedCardPublisher exportedCardPublisher;

    @MockBean
    StreamBridge streamBridge;
  }
}

