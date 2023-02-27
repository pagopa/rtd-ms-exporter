package it.pagopa.gov.rtdmsexporter.configuration;

import io.reactivex.rxjava3.core.Flowable;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.domain.ChunkWriter;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.MongoPagedCardReader;
import it.pagopa.gov.rtdmsexporter.infrastructure.ChunkBufferedWriter;
import it.pagopa.gov.rtdmsexporter.domain.ExportDatabaseStep;
import it.pagopa.gov.rtdmsexporter.domain.PagedCardReader;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import it.pagopa.gov.rtdmsexporter.infrastructure.step.ExportToFileStep;
import it.pagopa.gov.rtdmsexporter.infrastructure.step.SaveAcquirerFileStep;
import it.pagopa.gov.rtdmsexporter.infrastructure.step.ZipStep;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

@Configuration
public class ExportJobModule {

  private static final String COLLECTION_NAME = "enrolled_payment_instrument";
  private static final String ACQUIRER_GENERATED_FILE = "acquirer-cards.csv";
  private static final String ACQUIRER_ZIP_FILE = "acquirer-cards.zip";

  private final int readChunkSize;

  public ExportJobModule(
          @Value("${exporter.readChunkSize:10}") int readChunkSize
  ) {
    this.readChunkSize = readChunkSize;
  }

  @Bean
  ExportDatabaseStep exportDatabaseStep(
          Flowable<List<CardEntity>> source,
          Function<CardEntity, List<String>> flattenCardHashes,
          ChunkWriter<String> chunkWriter
  ) {
    return new ExportToFileStep(
            source,
            flattenCardHashes,
            chunkWriter,
            readChunkSize,
            4
    );
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
  Function<CardEntity, List<String>> flattenCardHashes() {
    return cardEntity -> Stream.concat(
            Stream.of(cardEntity.getHashPan()),
            cardEntity.getHashPanChildren().stream()
    ).toList();
  }

  @Bean
  Flowable<List<CardEntity>> batchCardReader(PagedCardReader cardReader) {
    return Flowable.<List<CardEntity>>generate(emitter -> {
      final var batch = cardReader.read();
      if (Objects.nonNull(batch) && !batch.isEmpty()) {
        emitter.onNext(batch);
      } else {
        emitter.onComplete();
      }
    }).onBackpressureBuffer().doFinally(cardReader::reset);
  }

  @Bean
  ChunkWriter<String> chunkBufferedWriter() {
    return new ChunkBufferedWriter(new File(ACQUIRER_GENERATED_FILE));
  }

  @Bean
  public PagedCardReader cardReader(MongoTemplate mongoTemplate) {
    final var query = new Query();
    query.fields().include("hashPan", "hashPanChildren", "par", "exportConfirmed");
    query.addCriteria(Criteria.where("state").is("NOT_ENROLLED"));
    return new MongoPagedCardReader(
            mongoTemplate,
            COLLECTION_NAME,
            query,
            "hashPan",
            Sort.Direction.ASC,
            readChunkSize
    );
  }
}
