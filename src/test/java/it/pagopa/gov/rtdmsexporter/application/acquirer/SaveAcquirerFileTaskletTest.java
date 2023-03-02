package it.pagopa.gov.rtdmsexporter.application.acquirer;

import it.pagopa.gov.rtdmsexporter.domain.acquirer.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@Import(SaveAcquirerFileTaskletTest.Config.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
class SaveAcquirerFileTaskletTest {

  private static final String TEST_FILE_TO_UPLOAD = "./fileToUpload.zip";

  @Autowired
  private AcquirerFileRepository acquirerFileRepository;

  private SaveAcquirerFileStep saveAcquirerFileStep;

  @BeforeEach
  void setup() {
    saveAcquirerFileStep = new SaveAcquirerFileStep(TEST_FILE_TO_UPLOAD, acquirerFileRepository);
  }

  @AfterEach
  void cleanup() throws IOException {
    reset(acquirerFileRepository);
    Files.deleteIfExists(Path.of(TEST_FILE_TO_UPLOAD));
  }

  @Test
  void whenFileToUploadExistsThenSaveIt() throws Exception {
    FileUtils.generateFile(TEST_FILE_TO_UPLOAD, Stream.of("text"));
    when(acquirerFileRepository.save(any())).thenReturn(true);
    assertThat(saveAcquirerFileStep.execute()).isTrue();
    verify(acquirerFileRepository, times(1)).save(any());
  }

  @Test
  void whenFileToUploadNotExistsThenTaskletFails() throws Exception {
    assertThat(saveAcquirerFileStep.execute()).isFalse();
    verify(acquirerFileRepository, times(0)).save(any());
  }

  @Test
  void whenFileFailToSaveThenTaskletFail() throws Exception {
    FileUtils.generateFile(TEST_FILE_TO_UPLOAD, Stream.of("text"));
    when(acquirerFileRepository.save(any())).thenReturn(false);
    assertThat(saveAcquirerFileStep.execute()).isFalse();
    verify(acquirerFileRepository, times(1)).save(any());
  }

  static class Config {
    @Bean
    AcquirerFileRepository acquirerFileRepository() {
      return mock(AcquirerFileRepository.class);
    }
  }

}