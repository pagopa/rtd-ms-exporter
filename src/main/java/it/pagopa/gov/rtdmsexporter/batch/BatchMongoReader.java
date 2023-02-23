package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.utils.PerformanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Slf4j
public class BatchMongoReader<T> implements ItemStreamReader<T> {

  private final MongoTemplate mongoTemplate;
  private final String collectionName;
  private final Query baseQuery;
  private final Class<? extends T> type;
  private final String keyName;
  private final String keyValue;
  private final int batchSize;

  public BatchMongoReader(
          MongoTemplate mongoTemplate,
          String collectionName,
          Query query,
          Class<? extends T> type,
          String keyName,
          String keyValue, int batchSize) {
    this.mongoTemplate = mongoTemplate;
    this.collectionName = collectionName;
    this.baseQuery = query;
    this.type = type;
    this.keyName = keyName;
    this.keyValue = keyValue;
    this.batchSize = batchSize;
  }

  private List<T> items;
  private int currentIndex = 0;

  @Override
  public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    if (items == null) {
      final var query = Query.of(baseQuery)
              .with(Sort.by(Sort.Direction.ASC, keyName))
              .limit(batchSize)
              .addCriteria(Criteria.where(keyName).gt(keyValue));

      items = (List<T>) PerformanceUtils.timeIt(
              "Query performance",
              () -> mongoTemplate.find(query, type, collectionName)
      );
      log.info("Size {}", items.size());

    }

    return currentIndex < items.size() ? items.get(currentIndex++) : null;
  }
}
