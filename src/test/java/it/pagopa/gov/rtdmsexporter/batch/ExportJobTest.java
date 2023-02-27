package it.pagopa.gov.rtdmsexporter.batch;


import com.mongodb.MongoException;
import it.pagopa.gov.rtdmsexporter.application.ExportJobService;
import it.pagopa.gov.rtdmsexporter.configuration.MockMongoConfiguration;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFile;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.ZipUtils;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import it.pagopa.gov.rtdmsexporter.utils.HashStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
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

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@Import(ExportJobTest.Config.class)
@ContextConfiguration(classes = { MockMongoConfiguration.class, ExportJobConfiguration.class, BatchConfiguration.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
class ExportJobTest {

  private static final String TEST_ACQUIRER_FILE = "output.csv";
  private static final String TEST_ZIP_ACQUIRER_FILE = "output.zip";

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private AcquirerFileRepository acquirerFileRepository;

  private JobParameters jobParameters;

  @BeforeEach
  void setup(@Autowired Job exportJob) {
    jobLauncherTestUtils.setJob(exportJob);
    jobParameters = new JobParametersBuilder()
            .addString(ExportJobService.TARGET_ACQUIRER_FILENAME_KEY, TEST_ACQUIRER_FILE)
            .addString(ExportJobService.TARGET_ACQUIRER_ZIP_KEY, TEST_ZIP_ACQUIRER_FILE)
            .toJobParameters();
    final var cards = HashStream.of(20)
            .map(it -> new CardEntity(it, HashStream.of(2, it).collect(Collectors.toList()), "", false))
            .collect(Collectors.toList());
    when(mongoTemplate.find(any(Query.class), eq(CardEntity.class), anyString())).thenReturn(cards);
  }

  @AfterEach
  void cleanup(@Autowired JobRepositoryTestUtils jobRepositoryTestUtils) throws IOException {
    Files.deleteIfExists(Path.of(TEST_ACQUIRER_FILE));
    Files.deleteIfExists(Path.of(TEST_ZIP_ACQUIRER_FILE));
    reset(mongoTemplate, acquirerFileRepository);
    jobRepositoryTestUtils.removeJobExecutions();
  }

  @Test
  void whenCardAvailableThenCompleteJob() throws Exception {
    final var captor = ArgumentCaptor.forClass(AcquirerFile.class);
    when(acquirerFileRepository.save(any())).thenReturn(true);
    final var result = jobLauncherTestUtils.launchJob(jobParameters);
    assertThat(result.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    verify(acquirerFileRepository, times(1)).save(captor.capture());
    assertThat(captor.getValue().file().getPath()).contains(".zip");
  }

  @Test
  void whenExportToFileFailThenSkipUploadAndJobFail() throws Exception {
    when(mongoTemplate.find(any(Query.class), eq(CardEntity.class), anyString())).thenThrow(new MongoException("Error"));
    assertThat(jobLauncherTestUtils.launchJob(jobParameters).getStatus()).isEqualTo(BatchStatus.FAILED);
    verify(acquirerFileRepository, times(0)).save(any());
  }

  @Test
  void whenZipFailThenSkipUploadAndFail() throws Exception {
    try (final var zipUtils = mockStatic(ZipUtils.class)) {
      zipUtils.when(() -> ZipUtils.zipFile(any(), any())).thenReturn(Optional.empty());
      assertThat(jobLauncherTestUtils.launchJob(jobParameters).getExitStatus()).isEqualTo(ExitStatus.FAILED);
      verify(acquirerFileRepository, times(0)).save(any());
    }
  }

  @Test
  void whenUploadFailThenJobFail() throws Exception {
    when(acquirerFileRepository.save(any())).thenReturn(false);
    assertThat(jobLauncherTestUtils.launchJob(jobParameters).getExitStatus()).isEqualTo(ExitStatus.FAILED);
  }

  @TestConfiguration
  static class Config {
    @MockBean
    private AcquirerFileRepository acquirerFileRepository;
  }
}

