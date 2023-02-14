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

  public ExportJobLauncher(JobLauncher jobLauncher, Job exportJob) {
    this.jobLauncher = jobLauncher;
    this.exportJob = exportJob;
  }

  public JobExecution execute() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    final var params = new JobParametersBuilder()
            .addString("jobId", UUID.randomUUID().toString())
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
    return jobLauncher.run(exportJob, params);
  }
}
