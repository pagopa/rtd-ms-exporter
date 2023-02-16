package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.KeyPageableEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.Iterator;

public class KeyPaginatedMongoReader<T extends KeyPageableEntity> extends AbstractPaginatedDataItemReader<T> {

  private final MongoTemplate mongoTemplate;
  private final String collectionName;
  private final Query baseQuery;
  private final Class<? extends T> type;
  private final String keyName;
  private final Sort.Direction sortDirection;

  private String startingObjectId;

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
    setName(ClassUtils.getShortName(KeyPaginatedMongoReader.class));
    initializeBaseQuery();
  }

  private void initializeBaseQuery() {
    baseQuery.with(Sort.by(sortDirection, keyName));
  }

  @Override
  public void setPageSize(int pageSize) {
    super.setPageSize(pageSize);
    baseQuery.limit(pageSize);
  }

  @NotNull
  @Override
  protected Iterator<T> doPageRead() {
    final var query = Query.of(baseQuery);
    // if is the 2nd page and startingKey is not null then paginate by that
    if (page > 0 && startingObjectId != null) {
      query.addCriteria(Criteria.where(keyName).gt(startingObjectId));
    }

    final var items = mongoTemplate.find(query, type, collectionName);

    if (!items.isEmpty()) {
      startingObjectId = items.get(items.size() - 1).getKey();
      return (Iterator<T>) items.iterator();
    }
    return Collections.emptyIterator();
  }
}
