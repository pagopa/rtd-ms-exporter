package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.application.MongoCardReader;
import it.pagopa.gov.rtdmsexporter.application.RxCardExportJob;
import it.pagopa.gov.rtdmsexporter.batch.tasklet.ZipTasklet;
import it.pagopa.gov.rtdmsexporter.domain.CardProcessor;
import it.pagopa.gov.rtdmsexporter.domain.CardReader;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.File;

@Configuration
@Slf4j
public class ExportJobConfiguration {

  private static final String COLLECTION_NAME = "enrolled_payment_instrument";
  public static final String JOB_NAME = "exportJob";
  public static final String EXPORT_TO_FILE_STEP = "exportToFileStep";
  public static final String ZIP_STEP = "zipStep";
  public static final String UPLOAD_STEP = "uploadStep";

  private final int readChunkSize;

  public ExportJobConfiguration(
          @Value("${exporter.readChunkSize:10}") int readChunkSize
  ) {
    this.readChunkSize = 10_000;
  }

  @Bean
  ExportJob exportJob(
          CardReader<CardEntity> cardReader,
          CardProcessor cardProcessor,
          ChunkBufferedWriter acquirerFileWriter,
          ZipTasklet zipTasklet
  ) {
    return new RxCardExportJob(
            cardReader,
            cardProcessor,
            acquirerFileWriter,
            zipTasklet,
            Runtime.getRuntime().availableProcessors()
    );
  }

  @Bean
  public CardReader<CardEntity> cardReader(MongoTemplate mongoTemplate) {
    final var query = new Query();
    query.fields().include("hashPan", "hashPanChildren", "par", "exportConfirmed");
    query.addCriteria(Criteria.where("state").is("NOT_ENROLLED"));
    return new MongoCardReader(
            mongoTemplate,
            COLLECTION_NAME,
            query,
            "hashPan",
            Sort.Direction.ASC,
            readChunkSize
    );
  }

  @Bean
  public CardProcessor cardProcessor() {
    return new CardProcessor();
  }

  @Bean
  public ChunkBufferedWriter acquirerFileWriter() throws Exception {
    return new ChunkBufferedWriter(new File("acquirer.csv"));
  }

  @Bean
  public ZipTasklet zipTasklet() {
    return new ZipTasklet("acquirer.csv", "hashPans.zip");
  }
}
