package it.pagopa.gov.rtdmsexporter.configuration;

import it.pagopa.gov.rtdmsexporter.batch.ExportJobLauncher;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.domain.CardExport;
import it.pagopa.gov.rtdmsexporter.infrastructure.LocalAcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.SetCardExport;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

  private static final String ACQUIRER_DOWNLOAD_FILE = "/Users/andrea.petreti/Development/rtd-ms-exporter/src/main/resources/prova.csv";

  @Bean
  public CardExport cardExport() {
    return new SetCardExport();
  }

  @Bean
  public AcquirerFileRepository acquirerFileRepository() {
    return new LocalAcquirerFileRepository(ACQUIRER_DOWNLOAD_FILE);
  }

  @Bean
  public ExportJobLauncher exportJobLauncher(JobLauncher jobLauncher, Job exportJob) {
    return new ExportJobLauncher(jobLauncher, exportJob, ACQUIRER_DOWNLOAD_FILE, 50);
  }

  @Bean
  public JobLauncher jobLauncher(JobRepository jobRepository) {
    final var jobLauncher = new TaskExecutorJobLauncher();
    jobLauncher.setJobRepository(jobRepository);
    return jobLauncher;
  }

  @Bean
  protected DataSource dataSource() {
    EmbeddedDatabaseBuilder embeddedDatabaseBuilder = new EmbeddedDatabaseBuilder();
    return embeddedDatabaseBuilder
            .addScript("classpath:org/springframework/batch/core/schema-drop-h2.sql")
            .addScript("classpath:org/springframework/batch/core/schema-h2.sql")
            .setType(EmbeddedDatabaseType.H2)
            .build();
  }
}
