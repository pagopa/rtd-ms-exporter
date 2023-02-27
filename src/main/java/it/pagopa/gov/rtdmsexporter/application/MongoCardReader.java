package it.pagopa.gov.rtdmsexporter.application;

import it.pagopa.gov.rtdmsexporter.domain.CardReader;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import it.pagopa.gov.rtdmsexporter.utils.PerformanceUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.Iterator;

public class MongoCardReader implements CardReader<CardEntity> {

  private final MongoTemplate mongoTemplate;
  private final String collectionName;
  private final Query baseQuery;
  private final String keyName;
  private final Sort.Direction sortDirection;
  private final int pageSize;

  private String startingNextKey;
  private boolean hasNextPage;

  public MongoCardReader(MongoTemplate mongoTemplate, String collectionName, Query baseQuery, String keyName, Sort.Direction sortDirection, int pageSize) {
    this.mongoTemplate = mongoTemplate;
    this.collectionName = collectionName;
    this.baseQuery = baseQuery;
    this.keyName = keyName;
    this.sortDirection = sortDirection;
    this.pageSize = pageSize;
    this.startingNextKey = null;
    this.hasNextPage = true;
  }

  @Override
  public Iterator<CardEntity> read() {
    final var query = Query.of(baseQuery);
    query.with(Sort.by(sortDirection, keyName));
    query.limit(pageSize);
    // if is the 2nd page and startingKey is not null then paginate by that
    synchronized (this) {
      if (hasNextPage) {
        if (startingNextKey != null) {
          query.addCriteria(Criteria.where(keyName).gt(startingNextKey));
        }

        final var items = PerformanceUtils.timeIt(
                "Query performance",
                () -> mongoTemplate.find(query, CardEntity.class, collectionName)
        );

        if (!items.isEmpty()) {
          startingNextKey = items.get(items.size() - 1).getKey();
          hasNextPage = items.size() == pageSize;
          return items.iterator();
        }
      }
      return Collections.emptyIterator();
    }
  }
}