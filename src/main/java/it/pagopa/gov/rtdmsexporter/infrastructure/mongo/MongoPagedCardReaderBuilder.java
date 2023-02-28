package it.pagopa.gov.rtdmsexporter.infrastructure.mongo;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

public class MongoPagedCardReaderBuilder {
  private MongoTemplate mongoTemplate;
  private String collectionName;
  private Query baseQuery;
  private String keyName;
  private Sort.Direction sortDirection;
  private int pageSize;

  public MongoPagedCardReaderBuilder setMongoTemplate(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
    return this;
  }

  public MongoPagedCardReaderBuilder setCollectionName(String collectionName) {
    this.collectionName = collectionName;
    return this;
  }

  public MongoPagedCardReaderBuilder setBaseQuery(Query baseQuery) {
    this.baseQuery = baseQuery;
    return this;
  }

  public MongoPagedCardReaderBuilder setKeyName(String keyName) {
    this.keyName = keyName;
    return this;
  }

  public MongoPagedCardReaderBuilder setSortDirection(Sort.Direction sortDirection) {
    this.sortDirection = sortDirection;
    return this;
  }

  public MongoPagedCardReaderBuilder setPageSize(int pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  public MongoPagedCardReader build() {
    return new MongoPagedCardReader(mongoTemplate, collectionName, baseQuery, keyName, sortDirection, pageSize);
  }
}