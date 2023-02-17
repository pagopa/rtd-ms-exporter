package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.configuration.BatchConfiguration;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import it.pagopa.gov.rtdmsexporter.utils.HashStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.*;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.pagopa.gov.rtdmsexporter.batch.ExportJobConfiguration.EXPORT_TO_FILE_STEP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@Import(ExportToFileStepTest.MockMongoConfiguration.class)
@ContextConfiguration(classes = {
        ExportJobConfiguration.class,
        BatchConfiguration.class
})
@TestExecutionListeners( {
        DependencyInjectionTestExecutionListener.class,
        StepScopeTestExecutionListener.class
})
class ExportToFileStepTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private JobRepositoryTestUtils jobRepositoryTestUtils;

  @Autowired
  private MongoTemplate mongoTemplate;

  private static final int TEST_CHUNK_SIZE = 10;
  private static final String TEST_ACQUIRER_FILE = "./test_out_acquirer.csv";

  @BeforeEach
  void setup() {

  }

  @AfterEach
  public void cleanUp() {
    jobRepositoryTestUtils.removeJobExecutions();
    new File(TEST_ACQUIRER_FILE).delete();
  }

  @Test
  void whenCardsAvailableThenWriteToFileFlatten() throws Exception {
    final var cards = HashStream.of(20)
            .map(it -> new CardEntity(it, HashStream.of(2, it).collect(Collectors.toList()), "", false))
            .collect(Collectors.toList());

    when(mongoTemplate.find(any(Query.class), eq(CardEntity.class), anyString())).thenReturn(cards);

    final var jobParameters = new JobParametersBuilder()
            .addLong("readChunkSize", (long) TEST_CHUNK_SIZE)
            .addString("acquirerFilename", TEST_ACQUIRER_FILE)
            .toJobParameters();

    // Run the job and check the result
    final var jobExecution = StepScopeTestUtils.doInStepScope(
            MetaDataInstanceFactory.createStepExecution(jobParameters),
            () -> jobLauncherTestUtils.launchStep(EXPORT_TO_FILE_STEP, jobParameters)
    );
    assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

    final var written = Files.readAllLines(Path.of(TEST_ACQUIRER_FILE));
    assertThat(written).hasSameElementsAs(
            cards.stream()
                    .flatMap(it -> Stream.concat(Stream.of(it.getHashPan()), it.getHashPanChildren().stream()))
                    .collect(Collectors.toList())
    );
  }



  @TestConfiguration
  static class MockMongoConfiguration {

    @Bean
    MongoTemplate mongoTemplate() {
      return Mockito.mock(MongoTemplate.class);
    }
  }
}