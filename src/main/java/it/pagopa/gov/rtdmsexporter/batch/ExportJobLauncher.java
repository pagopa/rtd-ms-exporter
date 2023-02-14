package it.pagopa.gov.rtdmsexporter.batch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import java.util.UUID;

public final class ExportJobLauncher {

  private final JobLauncher jobLauncher;
  private final Job exportJob;
  private final String fileToIndex;
  private final int indexingReadChunks;

  public ExportJobLauncher(
          JobLauncher jobLauncher,
          Job exportJob,
          String fileToIndex,
          int indexingReadChunks
  ) {
    this.jobLauncher = jobLauncher;
    this.exportJob = exportJob;
    this.fileToIndex = fileToIndex;
    this.indexingReadChunks = indexingReadChunks;
  }

  public JobExecution execute() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    final var params = new JobParametersBuilder()
            .addString("jobId", UUID.randomUUID().toString())
            .addLong("timestamp", System.currentTimeMillis())
            .addString("fileToIndex", fileToIndex)
            .addLong("chunks", (long) indexingReadChunks)
            .toJobParameters();
    return jobLauncher.run(exportJob, params);
  }
}
