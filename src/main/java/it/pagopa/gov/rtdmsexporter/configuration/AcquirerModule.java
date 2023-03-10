package it.pagopa.gov.rtdmsexporter.configuration;

import io.reactivex.rxjava3.core.Scheduler;
import it.pagopa.gov.rtdmsexporter.application.acquirer.AcquirerFileSubscriber;
import it.pagopa.gov.rtdmsexporter.application.acquirer.SaveAcquirerFileStep;
import it.pagopa.gov.rtdmsexporter.application.acquirer.ZipStep;
import it.pagopa.gov.rtdmsexporter.domain.acquirer.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.domain.acquirer.ChunkWriter;
import it.pagopa.gov.rtdmsexporter.infrastructure.ChunkBufferedWriter;
import it.pagopa.gov.rtdmsexporter.infrastructure.blob.BlobAcquirerRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.blob.BlobConfig;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Configuration
public class AcquirerModule {

  public static final String ACQUIRER_GENERATED_FILE = "./acquirer-cards.csv";
  private static final String ACQUIRER_ZIP_FILE = "./acquirer-cards.zip";

  private final int readChunkSize;

  public AcquirerModule(
          @Value("${exporter.readChunkSize:10}") int readChunkSize
  ) {
    this.readChunkSize = readChunkSize;
  }

  @Bean
  ZipStep zipStep() {
    return new ZipStep(ACQUIRER_GENERATED_FILE, ACQUIRER_ZIP_FILE);
  }

  @Bean
  SaveAcquirerFileStep saveAcquirerFileStep(
          AcquirerFileRepository acquirerFileRepository
  ) {
    return new SaveAcquirerFileStep(ACQUIRER_ZIP_FILE, acquirerFileRepository);
  }

  @Bean
  AcquirerFileSubscriber acquirerFileSubscriber(
          Scheduler rxScheduler,
          Function<CardEntity, List<String>> flattenCardHashes,
          ChunkWriter<String> chunkBufferedWriter
  ) {
    return new AcquirerFileSubscriber(flattenCardHashes, chunkBufferedWriter, rxScheduler, readChunkSize);
  }

  @Bean
  Function<CardEntity, List<String>> flattenCardHashes() {
    return cardEntity -> Stream.concat(
            Stream.of(cardEntity.getHashPan()),
            cardEntity.getHashPanChildren().stream()
    ).toList();
  }

  @Bean
  ChunkWriter<String> chunkBufferedWriter() {
    return new ChunkBufferedWriter(new File(ACQUIRER_GENERATED_FILE));
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
}
