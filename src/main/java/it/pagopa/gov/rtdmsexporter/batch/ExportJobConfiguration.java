package it.pagopa.gov.rtdmsexporter.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ExportJobConfiguration {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;

  public ExportJobConfiguration(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
    this.jobRepository = jobRepository;
    this.platformTransactionManager = platformTransactionManager;
  }

  @Bean
  Job exportJob(Step emptyStep) {
    return new JobBuilder("exportJob", jobRepository)
            .preventRestart()
            .start(emptyStep)
            .build();
  }

  @Bean
  Step emptyStep() {
    return new StepBuilder("emptyStep", jobRepository)
            .tasklet(new EmptyTasklet(), platformTransactionManager)
            .build();
  }
}
