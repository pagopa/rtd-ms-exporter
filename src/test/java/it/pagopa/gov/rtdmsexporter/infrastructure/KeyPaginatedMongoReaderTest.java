package it.pagopa.gov.rtdmsexporter.infrastructure;

import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.MongoPagedCardReader;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.MongoPagedCardReaderBuilder;
import it.pagopa.gov.rtdmsexporter.utils.HashStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Testcontainers
@DataMongoTest
@ExtendWith(SpringExtension.class)
class KeyPaginatedMongoReaderTest {

  @Container
  public static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:4.4.4");

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
  }

  @Autowired
  private MongoTemplate mongoTemplate;

  private MongoTemplate mongoTemplateSpy;
  private MongoPagedCardReader paginatedMongoReader;

  @BeforeEach
  void setup() {
    mongoTemplate.indexOps("cards")
            .ensureIndex(new Index().on("hashPan", Sort.Direction.ASC).unique());
    mongoTemplateSpy = Mockito.spy(mongoTemplate);
    paginatedMongoReader = new MongoPagedCardReaderBuilder()
            .setMongoTemplate(mongoTemplateSpy)
            .setKeyName("hashPan")
            .setCollectionName("cards")
            .setSortDirection(Sort.Direction.ASC)
            .setPageSize(10)
            .setBaseQuery(new Query())
            .build();
  }

  @AfterEach
  void clean() {
    mongoTemplate.dropCollection("cards");
  }

  @Test
  void whenThereIsItemThenContinueToReadAll() {
    // create cards
    HashStream.of(25)
            .map(it -> new CardEntity(it, Collections.emptyList(), "", false))
            .forEach(it -> mongoTemplate.save(it, "cards"));

    final var reads = Stream.generate(() -> Try.of(paginatedMongoReader::read))
            .map(Try::get)
            .takeWhile(it -> !it.isEmpty())
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    assertThat(reads).hasSize(25);
  }

  @Test
  void whenItemsAreSameOfPageSizeThenSecondReadMustBeEmpty() {
    HashStream.of(10)
            .map(it -> new CardEntity(it, Collections.emptyList(), "", false))
            .forEach(it -> mongoTemplate.save(it, "cards"));

    final var reads = Stream.generate(() -> Try.of(paginatedMongoReader::read))
            .map(Try::get)
            .takeWhile(it -> !it.isEmpty())
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    assertThat(reads).hasSize(10);
  }

  @Test
  void whenThereIsNoMoreItemThenLastQueryIsSkipped() {
    // create cards
    HashStream.of(25)
            .map(it -> new CardEntity(it, Collections.emptyList(), "", false))
            .forEach(it -> mongoTemplate.save(it, "cards"));

    final var readItems = Stream.generate(() -> Try.of(paginatedMongoReader::read))
            .map(Try::get)
            .takeWhile(it -> !it.isEmpty())
            .flatMap(Collection::stream)
            .count();

    assertThat(readItems).isEqualTo(25);
    verify(mongoTemplateSpy, times(3)).find(any(), any(), any());
  }

}