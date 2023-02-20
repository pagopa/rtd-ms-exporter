package it.pagopa.gov.rtdmsexporter.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.Date;

@Slf4j
public class ExportJobService {

  public static final String TARGET_ACQUIRER_FILENAME_KEY = "acquirerFilename";
  public static final String TARGET_ACQUIRER_ZIP_KEY = "zipFilename";

  private final JobLauncher jobLauncher;
  private final Job exportJob;

  private final String acquirerTargetFile;
  private final String acquirerZipTargetFile;

  public ExportJobService(JobLauncher jobLauncher, Job exportJob, String acquirerTargetFile, String acquirerZipTargetFile) {
    this.jobLauncher = jobLauncher;
    this.exportJob = exportJob;
    this.acquirerTargetFile = acquirerTargetFile;
    this.acquirerZipTargetFile = acquirerZipTargetFile;
  }

  public JobExecution execute() throws Exception {
    log.info("Export job started");
    final var parameters = new JobParametersBuilder()
            .addLong("timestamp", new Date().getTime())
            .addString(TARGET_ACQUIRER_FILENAME_KEY, acquirerTargetFile)
            .addString(TARGET_ACQUIRER_ZIP_KEY, acquirerZipTargetFile)
            .toJobParameters();
    final var execution = jobLauncher.run(exportJob, parameters);
    log.info("Export job ends at {}", execution.getEndTime());
    return execution;
  }

}
