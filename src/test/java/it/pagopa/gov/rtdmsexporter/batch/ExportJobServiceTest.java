package it.pagopa.gov.rtdmsexporter.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.MetaDataInstanceFactory;

import static it.pagopa.gov.rtdmsexporter.batch.ExportJobService.TARGET_ACQUIRER_FILENAME_KEY;
import static it.pagopa.gov.rtdmsexporter.batch.ExportJobService.TARGET_ACQUIRER_ZIP_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExportJobServiceTest {

  private JobLauncher jobLauncher;
  private ExportJobService exportJobService;

  private static final String TEST_ACQUIRER_FILE = "file.csv";
  private static final String TEST_ACQUIRER_ZIP_FILE = "file.zip";

  @BeforeEach
  void setup() {
    jobLauncher = mock(JobLauncher.class);
    exportJobService = new ExportJobService(
            jobLauncher,
            mock(Job.class),
            TEST_ACQUIRER_FILE,
            TEST_ACQUIRER_ZIP_FILE
    );
  }

  @Test
  void shouldLaunchJobWithProperParameters() throws Exception {
    final var parametersCaptor = ArgumentCaptor.forClass(JobParameters.class);
    when(jobLauncher.run(any(), any())).thenReturn(MetaDataInstanceFactory.createJobExecution());

    exportJobService.execute();

    verify(jobLauncher).run(any(), parametersCaptor.capture());
    assertThat(parametersCaptor.getValue())
            .satisfies(it -> assertThat(it.getString(TARGET_ACQUIRER_FILENAME_KEY)).isEqualTo(TEST_ACQUIRER_FILE))
            .satisfies(it -> assertThat(it.getString(TARGET_ACQUIRER_ZIP_KEY)).isEqualTo(TEST_ACQUIRER_ZIP_FILE));
  }

}