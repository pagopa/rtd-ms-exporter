package it.pagopa.gov.rtdmsexporter.configuration;

import it.pagopa.gov.rtdmsexporter.batch.ExportJobService;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.BlobAcquirerRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.BlobConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

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
          @Value("${blobstorage.api.filename}") String filename,
          CloseableHttpClient httpClient
  ) {
    return new BlobAcquirerRepository(
            new BlobConfig(baseUrl, apiKey, containerName),
            filename,
            httpClient
    );
  }

  @Bean
  public CloseableHttpClient httpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    final var sslContext = SSLContexts.custom()
            .loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
            .build();

    final var registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE)
            .register("https",
                    new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
            .build();

    final var connectionManager = new PoolingHttpClientConnectionManager(
            registry);

    return HttpClients.custom().setConnectionManager(connectionManager).build();
  }
}
