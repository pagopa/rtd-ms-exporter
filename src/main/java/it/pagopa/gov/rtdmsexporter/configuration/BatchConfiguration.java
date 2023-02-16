package it.pagopa.gov.rtdmsexporter.configuration;

import it.pagopa.gov.rtdmsexporter.batch.ExportJobService;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.domain.CardExport;
import it.pagopa.gov.rtdmsexporter.infrastructure.BlobAcquirerRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.SetCardExport;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

  private static final String ACQUIRER_DOWNLOAD_FILE = "acquirer-cards.csv";

  @Bean
  public ExportJobService exportJobService(
          JobLauncher jobLauncher,
          Job jobExport,
          @Value("${exporter.readChunkSize}") int readChunkSize
  ) {
    return new ExportJobService(jobLauncher, jobExport, ACQUIRER_DOWNLOAD_FILE, readChunkSize);
  }

  @Bean
  public CardExport cardExport() {
    return new SetCardExport();
  }

  @Bean
  public AcquirerFileRepository acquirerFileRepository(
          @Value("${blobstorage.api.baseUrl}") String baseUrl,
          @Value("${blobstorage.api.filename}") String filename,
          @Value("${blobstorage.api.apiKey}") String apiKey
  ) {
    return new BlobAcquirerRepository(
            ACQUIRER_DOWNLOAD_FILE,
            baseUrl,
            filename,
            apiKey,
            HttpClientBuilder.create().build()
    );
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

  @Bean
  public JobRepository jobRepository(DataSource dataSource) throws Exception {
    JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
    factory.setDataSource(dataSource);
    factory.setTransactionManager(new ResourcelessTransactionManager());
    factory.afterPropertiesSet();
    return factory.getObject();
  }
}
