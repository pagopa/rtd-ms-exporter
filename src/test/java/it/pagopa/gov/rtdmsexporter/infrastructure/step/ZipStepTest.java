package it.pagopa.gov.rtdmsexporter.infrastructure.step;

import it.pagopa.gov.rtdmsexporter.infrastructure.ZipUtils;
import it.pagopa.gov.rtdmsexporter.infrastructure.step.ZipStep;
import it.pagopa.gov.rtdmsexporter.utils.FileUtils;
import it.pagopa.gov.rtdmsexporter.utils.HashStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
class ZipStepTest {

  private static final String TEST_ACQUIRER_FILE = "./test_out_acquirer.csv";
  private static final String TEST_ZIP_ACQUIRER_FILE = "./test_out_acquirer.zip";

  private ZipStep zipStep;

  @BeforeEach
  void setup() {
    zipStep = new ZipStep(TEST_ACQUIRER_FILE, TEST_ZIP_ACQUIRER_FILE);
  }

  @AfterEach
  void cleanup() throws IOException {
    Files.deleteIfExists(Path.of(TEST_ACQUIRER_FILE));
    Files.deleteIfExists(Path.of(TEST_ZIP_ACQUIRER_FILE));
  }

  @Test
  void whenFileToZipExistsThenZipIt() throws Exception {
    FileUtils.generateFile(TEST_ACQUIRER_FILE, HashStream.of(12));
    assertThat(zipStep.execute()).isTrue();
    assertThat(Files.exists(Path.of(TEST_ZIP_ACQUIRER_FILE))).isTrue();
  }

  @Test
  void whenFileToZipDoesntExistsThenFail() throws Exception {
    assertThat(zipStep.execute()).isFalse();
  }

  @Test
  void whenZippingFailThenTaskletFail() throws Exception {
    FileUtils.generateFile(TEST_ACQUIRER_FILE, HashStream.of(20));
    try (final var zipUtils = mockStatic(ZipUtils.class)) {
      zipUtils.when(() -> ZipUtils.zipFile(any(), any())).thenReturn(Optional.empty());
      assertThat(zipStep.execute()).isFalse();
    }
  }
}