package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.KeyPageableEntity;
import it.pagopa.gov.rtdmsexporter.utils.PerformanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.Iterator;

@Slf4j
public class KeyPaginatedMongoReader<T extends KeyPageableEntity> extends AbstractPaginatedDataItemReader<T> {

  private final MongoTemplate mongoTemplate;
  private final String collectionName;
  private final Query baseQuery;
  private final Class<? extends T> type;
  private final String keyName;
  private final Sort.Direction sortDirection;

  private String startingNextKey;
  private boolean hasNextPage;

  public KeyPaginatedMongoReader(
          MongoTemplate mongoTemplate,
          String collectionName,
          Query query,
          Class<? extends T> type,
          String keyName,
          Sort.Direction sortDirection
  ) {
    this.mongoTemplate = mongoTemplate;
    this.collectionName = collectionName;
    this.baseQuery = query;
    this.type = type;
    this.keyName = keyName;
    this.sortDirection = sortDirection;
    this.hasNextPage = true;
    setName(ClassUtils.getShortName(KeyPaginatedMongoReader.class));
  }

  @Override
  protected Iterator<T> doPageRead() {
    final var query = Query.of(baseQuery);
    query.with(Sort.by(sortDirection, keyName));
    query.limit(pageSize);
    // if is the 2nd page and startingKey is not null then paginate by that
    if (hasNextPage) {
      if (page > 0 && startingNextKey != null) {
        query.addCriteria(Criteria.where(keyName).gt(startingNextKey));
      }

      final var items = PerformanceUtils.timeIt(
              "Query performance",
              () -> mongoTemplate.find(query, type, collectionName)
      );

      if (!items.isEmpty()) {
        startingNextKey = items.get(items.size() - 1).getKey();
        hasNextPage = items.size() == pageSize;
        return (Iterator<T>) items.iterator();
      }
    }
    return Collections.emptyIterator();
  }
}
