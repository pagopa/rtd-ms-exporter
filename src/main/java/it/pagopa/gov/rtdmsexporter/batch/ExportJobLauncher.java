package it.pagopa.gov.rtdmsexporter.batch;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

public final class ExportJobLauncher {

  private final JobLauncher jobLauncher;
  private final Job exportJob;

  public ExportJobLauncher(JobLauncher jobLauncher, Job exportJob) {
    this.jobLauncher = jobLauncher;
    this.exportJob = exportJob;
  }

  public JobExecution execute() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    return jobLauncher.run(exportJob, new JobParameters());
  }
}
