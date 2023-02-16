package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import org.apache.logging.log4j.util.Strings;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
public class ExportJobConfiguration {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  public ExportJobConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  @Bean
  Job exportJob(Step readMongoDBStep) {
    return new JobBuilder("exportJob", jobRepository)
            .preventRestart()
            .incrementer(new RunIdIncrementer())
            .start(readMongoDBStep)
            .build();
  }

  @Bean
  @JobScope
  public Step readMongoDBStep(
          @Value("#{jobParameters[readChunkSize]}") int readChunkSize
  ) throws Exception {
    return new StepBuilder("readMongoDBStep", jobRepository)
            .<CardEntity, List<String>>chunk(readChunkSize, transactionManager)
            .reader(mongoItemReader(null, readChunkSize))
            .processor(cardFlatProcessor())
            .writer(acquirerFileWriter(null))
            .build();
  }

  @Bean
  @StepScope
  public KeyPaginatedMongoReader<CardEntity> mongoItemReader(
          MongoTemplate mongoTemplate,
          @Value("#{jobParameters[readChunkSize]}") int readChunkSize
  ) {
    final var query = new Query();
    query.fields().include("hashPan", "hashPanChildren", "par", "exportConfirmed");
    return new KeyPaginatedMongoReaderBuilder<CardEntity>()
            .setMongoTemplate(mongoTemplate)
            .setCollectionName("enrolled_payment_instrument")
            .setType(CardEntity.class)
            .setKeyName("hashPan")
            .setSortDirection(Sort.Direction.ASC)
            .setQuery(query)
            .setPageSize(readChunkSize)
            .build();
  }

  @Bean
  public ItemProcessor<CardEntity, List<String>> cardFlatProcessor() {
    return item -> {
      final var hashes = Optional.ofNullable(item.getHashPanChildren()).orElse(new ArrayList<>());
      hashes.add(item.getHashPan());
      return hashes;
    };
  }

  @Bean
  @StepScope
  public FlatFileItemWriter<List<String>> acquirerFileWriter(
          @Value("#{jobParameters[acquirerFilename]}") String acquirerFilename
  ) throws Exception {
    final var fileWriter = new FlatFileItemWriter<List<String>>();
    fileWriter.setResource(new FileSystemResource(acquirerFilename));
    fileWriter.setAppendAllowed(false);
    fileWriter.setShouldDeleteIfExists(true);
    fileWriter.setForceSync(true);
    fileWriter.setLineAggregator(item -> Strings.join(item, '\n'));
    fileWriter.afterPropertiesSet();
    return fileWriter;
  }
}
