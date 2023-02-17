package it.pagopa.gov.rtdmsexporter.configuration;

import it.pagopa.gov.rtdmsexporter.batch.ExportJobService;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.BlobAcquirerRepository;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

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
  public AcquirerFileRepository acquirerFileRepository(
          @Value("${blobstorage.api.baseUrl}") String baseUrl,
          @Value("${blobstorage.api.filename}") String filename,
          @Value("${blobstorage.api.apiKey}") String apiKey
  ) {
    return new BlobAcquirerRepository(
            baseUrl,
            filename,
            apiKey,
            HttpClientBuilder.create().build()
    );
  }
}
