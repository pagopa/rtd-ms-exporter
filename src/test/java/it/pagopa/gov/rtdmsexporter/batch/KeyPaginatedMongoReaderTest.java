package it.pagopa.gov.rtdmsexporter.batch;

import com.github.tonivade.purefun.type.Try;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataMongoTest
@ExtendWith(SpringExtension.class)
class KeyPaginatedMongoReaderTest {

  @Autowired
  private MongoTemplate mongoTemplate;

  private MongoTemplate mongoTemplateSpy;
  private KeyPaginatedMongoReader<CardEntity> paginatedMongoReader;

  @BeforeEach
  void setup() {
    mongoTemplate.indexOps("cards")
            .ensureIndex(new Index().on("hashPan", Sort.Direction.ASC).unique());
    mongoTemplateSpy = Mockito.spy(mongoTemplate);
    paginatedMongoReader = new KeyPaginatedMongoReaderBuilder<CardEntity>()
            .setMongoTemplate(mongoTemplateSpy)
            .setKeyName("hashPan")
            .setCollectionName("cards")
            .setSortDirection(Sort.Direction.ASC)
            .setType(CardEntity.class)
            .setPageSize(10)
            .setQuery(new Query())
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
            .map(Try::getOrElseNull)
            .takeWhile(Objects::nonNull)
            .collect(Collectors.toSet());

    assertThat(reads).hasSize(25);
  }

  @Test
  void whenThereIsNoMoreItemThenLastQueryIsSkipped() {
    // create cards
    HashStream.of(25)
            .map(it -> new CardEntity(it, Collections.emptyList(), "", false))
            .forEach(it -> mongoTemplate.save(it, "cards"));

    final var readItems = Stream.generate(() -> Try.of(paginatedMongoReader::read))
            .map(Try::getOrElseNull)
            .takeWhile(Objects::nonNull)
            .count();

    assertThat(readItems).isEqualTo(25);
    verify(mongoTemplateSpy, times(3)).find(any(), any(), any());
  }

}