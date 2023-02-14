package it.pagopa.gov.rtdmsexporter.batch;


import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.transaction.PlatformTransactionManager;

public class ExportJob {

  private final JobLauncher jobLauncher;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  public ExportJob(JobLauncher jobLauncher, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    this.jobLauncher = jobLauncher;
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  public JobExecution execute() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
    final var job = new JobBuilder("exportJob", jobRepository)
            .preventRestart()
            .start(new StepBuilder("emptyStep", jobRepository).tasklet(new EmptyTasklet(), transactionManager).build())
            .build();
    return jobLauncher.run(job, new JobParameters());
  }
}
