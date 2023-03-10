package it.pagopa.gov.rtdmsexporter.configuration;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.pagopa.gov.rtdmsexporter.application.ExportJob;
import it.pagopa.gov.rtdmsexporter.application.ExportJobService;
import it.pagopa.gov.rtdmsexporter.application.PagedDatabaseExportStep;
import it.pagopa.gov.rtdmsexporter.application.acquirer.SaveAcquirerFileStep;
import it.pagopa.gov.rtdmsexporter.application.acquirer.ZipStep;
import it.pagopa.gov.rtdmsexporter.application.paymentinstrument.NewExportedNotifyStep;
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
import java.util.concurrent.Executors;

@Configuration
public class AppConfiguration {

  private final int corePoolSize;

  public AppConfiguration(
          @Value("${exporter.corePoolSize:4}") int corePoolSize
  ) {
    this.corePoolSize = corePoolSize;
  }

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
  Scheduler rxScheduler() {
    return Schedulers.from(Executors.newFixedThreadPool(corePoolSize));
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
