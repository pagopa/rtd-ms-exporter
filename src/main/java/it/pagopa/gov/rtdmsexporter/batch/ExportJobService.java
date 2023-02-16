package it.pagopa.gov.rtdmsexporter.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

@Slf4j
public class ExportJobService {

  public static final String TARGET_ACQUIRER_FILENAME_KEY = "acquirerFilename";
  public static final String READ_CHUNK_SIZE_KEY = "readChunkSize";

  private final JobLauncher jobLauncher;
  private final Job exportJob;

  private final String acquirerTargetFile;
  private final int readChunkSize;

  public ExportJobService(JobLauncher jobLauncher, Job exportJob, String acquirerTargetFile, int readChunkSize) {
    this.jobLauncher = jobLauncher;
    this.exportJob = exportJob;
    this.acquirerTargetFile = acquirerTargetFile;
    this.readChunkSize = readChunkSize;
  }

  public JobExecution execute() throws Exception {
    log.info("Export job started");
    final var parameters = new JobParametersBuilder()
            .addString(TARGET_ACQUIRER_FILENAME_KEY, acquirerTargetFile)
            .addLong(READ_CHUNK_SIZE_KEY, (long) readChunkSize)
            .toJobParameters();
    final var execution = jobLauncher.run(exportJob, parameters);
    log.info("Export job ends at {}", execution.getEndTime());
    return execution;
  }

}
