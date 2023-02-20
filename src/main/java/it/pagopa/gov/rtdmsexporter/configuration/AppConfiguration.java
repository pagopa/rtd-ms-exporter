package it.pagopa.gov.rtdmsexporter.configuration;

import it.pagopa.gov.rtdmsexporter.batch.ExportJobService;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.BlobAcquirerRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.BlobConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

  private static final String ACQUIRER_GENERATED_FILE = "acquirer-cards.csv";
  private static final String ACQUIRER_ZIP_FILE = "acquirer-cards.zip";

  @Bean
  public ExportJobService exportJobService(
          JobLauncher jobLauncher,
          Job jobExport
  ) {
    return new ExportJobService(jobLauncher, jobExport, ACQUIRER_GENERATED_FILE, ACQUIRER_ZIP_FILE);
  }

  @Bean
  public AcquirerFileRepository acquirerFileRepository(
          @Value("${blobstorage.api.baseUrl}") String baseUrl,
          @Value("${blobstorage.api.apiKey}") String apiKey,
          @Value("${blobstorage.api.containerName}") String containerName,
          @Value("${blobstorage.api.filename}") String filename
  ) {
    return new BlobAcquirerRepository(
            new BlobConfig(baseUrl, apiKey, containerName),
            filename,
            HttpClientBuilder.create().build()
    );
  }
}
