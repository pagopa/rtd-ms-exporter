package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.configuration.BatchConfiguration;
import it.pagopa.gov.rtdmsexporter.infrastructure.ZipUtils;
import it.pagopa.gov.rtdmsexporter.utils.FileUtils;
import it.pagopa.gov.rtdmsexporter.utils.HashStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static it.pagopa.gov.rtdmsexporter.batch.ExportJobConfiguration.ZIP_STEP;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = { ExportJobConfiguration.class, BatchConfiguration.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
class ZipStepTest {

  private static final String TEST_ACQUIRER_FILE = "./test_out_acquirer.csv";
  private static final String TEST_ZIP_ACQUIRER_FILE = "./test_out_acquirer.zip";

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private JobRepositoryTestUtils jobRepositoryTestUtils;

  private JobParameters jobParameters;

  @BeforeEach
  void setup() {
    jobParameters = new JobParametersBuilder()
            .addString("acquirerFilename", TEST_ACQUIRER_FILE)
            .addString("zipFilename", TEST_ZIP_ACQUIRER_FILE)
            .toJobParameters();
  }

  @AfterEach
  void cleanup() throws IOException {
    Files.deleteIfExists(Path.of(TEST_ACQUIRER_FILE));
    Files.deleteIfExists(Path.of(TEST_ZIP_ACQUIRER_FILE));
    jobRepositoryTestUtils.removeJobExecutions();
  }

  @Test
  void whenFileToZipExistsThenZipIt() throws Exception {
    FileUtils.generateFile(TEST_ACQUIRER_FILE, HashStream.of(12));
    final var jobExecution = jobLauncherTestUtils.launchStep(ZIP_STEP, jobParameters);
    assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    assertThat(Files.exists(Path.of(TEST_ZIP_ACQUIRER_FILE))).isTrue();
  }

  @Test
  void whenFileToZipDoesntExistsThenFail() {
    final var jobExecution = jobLauncherTestUtils.launchStep(ZIP_STEP, jobParameters);
    assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.FAILED);
  }

  @Test
  void whenZippingFailThenTaskletFail() throws Exception {
    FileUtils.generateFile(TEST_ACQUIRER_FILE, HashStream.of(20));
    try (final var zipUtils = mockStatic(ZipUtils.class)) {
      zipUtils.when(() -> ZipUtils.zipFile(any(), any())).thenReturn(Optional.empty());
      final var jobExecution = jobLauncherTestUtils.launchStep(ZIP_STEP, jobParameters);
      assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.FAILED);
    }
  }
}