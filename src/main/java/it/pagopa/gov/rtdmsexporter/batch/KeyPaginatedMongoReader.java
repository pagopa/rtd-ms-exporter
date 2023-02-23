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
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class KeyPaginatedMongoReader<T> implements ItemStreamReader<List<T>> {

  private final MongoTemplate mongoTemplate;
  private final String collectionName;
  private final Query baseQuery;
  private final Class<? extends T> type;
  private final String keyName;
  private final Sort.Direction sortDirection;

  private final ConcurrentLinkedQueue<String> partitions;

  public KeyPaginatedMongoReader(
          MongoTemplate mongoTemplate,
          String collectionName,
          Query query,
          Class<? extends T> type,
          String keyName,
          Sort.Direction sortDirection,
          List<String> partitions
  ) {
    this.mongoTemplate = mongoTemplate;
    this.collectionName = collectionName;
    this.baseQuery = query;
    this.type = type;
    this.keyName = keyName;
    this.sortDirection = sortDirection;
    this.partitions = new ConcurrentLinkedQueue<>(partitions);
  }

  @Override
  public List<T> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    final var key = partitions.poll();
    if (key != null) {
      final var query = Query.of(baseQuery)
              .with(Sort.by(Sort.Direction.ASC, keyName))
              .limit(10_000);

      if (!key.equals("first_page")) {
        query.addCriteria(Criteria.where(keyName).gt(key));
      }

      final var items = PerformanceUtils.timeIt(
              "Query performance",
              () -> mongoTemplate.find(query, type, collectionName)
      );

      return (List<T>) items;
    } else {
      return null;
    }
  }
}
