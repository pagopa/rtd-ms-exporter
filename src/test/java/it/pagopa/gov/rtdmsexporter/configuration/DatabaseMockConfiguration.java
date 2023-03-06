package it.pagopa.gov.rtdmsexporter.configuration;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@TestConfiguration
public class DatabaseMockConfiguration {
  @Bean
  MongoTemplate mongoTemplate() {
    return Mockito.mock(MongoTemplate.class);
  }
}
