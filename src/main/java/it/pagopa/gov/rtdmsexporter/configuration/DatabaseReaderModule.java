package it.pagopa.gov.rtdmsexporter.configuration;

import io.reactivex.rxjava3.core.Scheduler;
import it.pagopa.gov.rtdmsexporter.application.PagedDatabaseExportStep;
import it.pagopa.gov.rtdmsexporter.application.acquirer.AcquirerFileSubscriber;
import it.pagopa.gov.rtdmsexporter.application.paymentinstrument.NewExportedSubscriber;
import it.pagopa.gov.rtdmsexporter.domain.PagedCardReader;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.MongoPagedCardReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Configuration
public class DatabaseReaderModule {

  private static final String COLLECTION_NAME = "enrolled_payment_instrument";

  private final int readChunkSize;

  public DatabaseReaderModule(
          @Value("${exporter.readChunkSize:10}") int readChunkSize
  ) {
    this.readChunkSize = readChunkSize;
  }

  @Bean
  PagedDatabaseExportStep exportDatabaseStep(
          Scheduler rxScheduler,
          PagedCardReader pagedCardReader,
          AcquirerFileSubscriber acquirerFileSubscriber,
          NewExportedSubscriber newExportedSubscriber
  ) {
    return new PagedDatabaseExportStep(
            rxScheduler,
            pagedCardReader,
            acquirerFileSubscriber::apply,
            newExportedSubscriber::apply
    );
  }

  @Bean
  PagedCardReader cardReader(MongoTemplate mongoTemplate) {
    final var query = new Query();
    query.fields().include("hashPan", "hashPanChildren", "par", "exported");
    query.addCriteria(Criteria.where("state").is("READY"));
    return new MongoPagedCardReaderBuilder()
            .setMongoTemplate(mongoTemplate)
            .setCollectionName(COLLECTION_NAME)
            .setBaseQuery(query)
            .setKeyName("hashPan")
            .setSortDirection(Sort.Direction.ASC)
            .setPageSize(readChunkSize)
            .build();
  }
}
