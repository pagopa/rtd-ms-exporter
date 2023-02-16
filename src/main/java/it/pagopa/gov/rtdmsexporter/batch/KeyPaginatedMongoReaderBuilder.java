package it.pagopa.gov.rtdmsexporter.batch;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

public class KeyPaginatedMongoReaderBuilder<T> {
  private MongoTemplate mongoTemplate;
  private String collectionName;
  private Query query;
  private Class<? extends T> type;
  private String keyName;
  private Sort.Direction sortDirection;
  private int pageSize;

  public KeyPaginatedMongoReaderBuilder<T> setMongoTemplate(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
    return this;
  }

  public KeyPaginatedMongoReaderBuilder<T> setCollectionName(String collectionName) {
    this.collectionName = collectionName;
    return this;
  }

  public KeyPaginatedMongoReaderBuilder<T> setQuery(Query query) {
    this.query = query;
    return this;
  }

  public KeyPaginatedMongoReaderBuilder<T> setType(Class<? extends T> type) {
    this.type = type;
    return this;
  }

  public KeyPaginatedMongoReaderBuilder<T> setKeyName(String keyName) {
    this.keyName = keyName;
    return this;
  }

  public KeyPaginatedMongoReaderBuilder<T> setSortDirection(Sort.Direction sortDirection) {
    this.sortDirection = sortDirection;
    return this;
  }

  public KeyPaginatedMongoReaderBuilder<T> setPageSize(int pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  public KeyPaginatedMongoReader<T> build() {
    final var reader = new KeyPaginatedMongoReader<T>(mongoTemplate, collectionName, query, type, keyName, sortDirection);
    reader.setPageSize(pageSize);
    return reader;
  }
}