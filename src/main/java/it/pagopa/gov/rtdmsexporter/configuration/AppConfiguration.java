package it.pagopa.gov.rtdmsexporter.configuration;

import it.pagopa.gov.rtdmsexporter.application.ExportJob;
import it.pagopa.gov.rtdmsexporter.application.ExportJobService;
import it.pagopa.gov.rtdmsexporter.application.PagedDatabaseExportStep;
import it.pagopa.gov.rtdmsexporter.application.paymentinstrument.NewExportedNotifyStep;
import it.pagopa.gov.rtdmsexporter.domain.acquirer.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.blob.BlobAcquirerRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.blob.BlobConfig;
import it.pagopa.gov.rtdmsexporter.application.acquirer.SaveAcquirerFileStep;
import it.pagopa.gov.rtdmsexporter.application.acquirer.ZipStep;
import it.pagopa.gov.rtdmsexporter.infrastructure.paymentinstrument.MemoryExportedCardRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class AppConfiguration {

  @Bean
  ExportJobService exportJobService(
          PagedDatabaseExportStep exportDatabaseStep,
          ZipStep zipStep,
          SaveAcquirerFileStep saveAcquirerFileStep,
          NewExportedNotifyStep exportedNotifyStep
  ) {
    return new ExportJobService(new ExportJob(exportDatabaseStep, zipStep, saveAcquirerFileStep, exportedNotifyStep));
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
            BlobConfig.of(baseUrl, apiKey, containerName),
            filename,
            httpClient
    );
  }

  @Bean
  public ExportedCardRepository exportedCardRepository() {
    return new MemoryExportedCardRepository();
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
