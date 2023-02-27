package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.utils.FileUtils;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static it.pagopa.gov.rtdmsexporter.batch.ExportJobConfiguration.UPLOAD_STEP;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@Import(SaveAcquirerFileTaskletTest.Config.class)
@ContextConfiguration(classes = { ExportJobConfiguration.class, BatchConfiguration.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
class SaveAcquirerFileTaskletTest {

  private static final String TEST_FILE_TO_UPLOAD = "./fileToUpload.zip";

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private AcquirerFileRepository acquirerFileRepository;
  private JobParameters jobParameters;

  @BeforeEach
  void setup() {
    jobParameters = new JobParametersBuilder()
            .addString("zipFilename", TEST_FILE_TO_UPLOAD)
            .toJobParameters();
  }

  @AfterEach
  void cleanup(@Autowired JobRepositoryTestUtils jobRepositoryTestUtils) throws IOException {
    jobRepositoryTestUtils.removeJobExecutions();
    reset(acquirerFileRepository);
    Files.deleteIfExists(Path.of(TEST_FILE_TO_UPLOAD));
  }

  @Test
  void whenFileToUploadExistsThenSaveIt() throws Exception {
    FileUtils.generateFile(TEST_FILE_TO_UPLOAD, Stream.of("text"));
    when(acquirerFileRepository.save(any())).thenReturn(true);
    final var jobExecution = jobLauncherTestUtils.launchStep(UPLOAD_STEP, jobParameters);
    assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    verify(acquirerFileRepository, times(1)).save(any());
  }

  @Test
  void whenFileToUploadNotExistsThenTaskletFails() {
    final var jobExecution = jobLauncherTestUtils.launchStep(UPLOAD_STEP, jobParameters);
    assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.FAILED);
    verify(acquirerFileRepository, times(0)).save(any());
  }

  @Test
  void whenFileFailToSaveThenTaskletFail() throws Exception {
    FileUtils.generateFile(TEST_FILE_TO_UPLOAD, Stream.of("text"));
    when(acquirerFileRepository.save(any())).thenReturn(false);
    final var jobExecution = jobLauncherTestUtils.launchStep(UPLOAD_STEP, jobParameters);
    assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.FAILED);
    verify(acquirerFileRepository, times(1)).save(any());
  }

  static class Config {
    @Bean
    AcquirerFileRepository acquirerFileRepository() {
      return mock(AcquirerFileRepository.class);
    }
  }

}