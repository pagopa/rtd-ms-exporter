package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.batch.listener.PerformanceWriterMonitor;
import it.pagopa.gov.rtdmsexporter.batch.tasklet.SaveAcquirerFileTasklet;
import it.pagopa.gov.rtdmsexporter.batch.tasklet.ZipTasklet;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamWriter;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.*;

@Configuration
@Slf4j
public class ExportJobConfiguration {

  private static final String COLLECTION_NAME = "enrolled_payment_instrument";
  public static final String JOB_NAME = "exportJob";
  public static final String EXPORT_TO_FILE_STEP = "exportToFileStep";
  public static final String ZIP_STEP = "zipStep";
  public static final String UPLOAD_STEP = "uploadStep";

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final int readChunkSize;

  public ExportJobConfiguration(
          JobRepository jobRepository,
          PlatformTransactionManager transactionManager,
          @Value("${exporter.readChunkSize:10}") int readChunkSize
  ) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
    this.readChunkSize = readChunkSize;
  }

  @Bean
  Job exportJob(Step readMongoDBStep, Step zipStep, Step uploadStep) {
    return new JobBuilder(JOB_NAME, jobRepository)
            .preventRestart()
            .incrementer(new RunIdIncrementer())
            .start(readMongoDBStep)
            .on(ExitStatus.COMPLETED.getExitCode())
            .to(zipStep)
            .on(ExitStatus.COMPLETED.getExitCode())
            .to(uploadStep)
            .build()
            .build();
  }

  @Bean
  public Step readMongoDBStep(TaskExecutor taskExecutor) throws Exception {
    return new StepBuilder(EXPORT_TO_FILE_STEP, jobRepository)
            .<CardEntity, List<String>>chunk(readChunkSize, transactionManager)
            .reader(mongoItemReader(null))
            .processor(cardFlatProcessor())
            .writer(acquirerFileWriter(null))
            .taskExecutor(taskExecutor)
            .listener(new PerformanceWriterMonitor<>())
            //.listener(new WorkloadDistributionListener<>())
            .build();
  }

  @Bean
  public TaskExecutor taskExecutor() {
    // Number of Cores * [ 1+ (wait time/CPU time)]
    final var cpus = Runtime.getRuntime().availableProcessors();
    final var corePoolSize = 8;
    final var executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize((int) corePoolSize);
    executor.setMaxPoolSize((int) corePoolSize);
    executor.setPrestartAllCoreThreads(true);
    executor.afterPropertiesSet();
    executor.initialize();
    log.info("Using executor with pool size {}", corePoolSize);
    return executor;
  }

  @Bean
  public Step zipStep(
          Tasklet zipTasklet
  ) {
    return new StepBuilder(ZIP_STEP, jobRepository)
            .tasklet(zipTasklet, transactionManager)
            .build();
  }

  @Bean
  public Step uploadStep(
          Tasklet saveAcquirerFileTasklet
  ) {
    return new StepBuilder(UPLOAD_STEP, jobRepository)
            .tasklet(saveAcquirerFileTasklet, transactionManager)
            .build();
  }

  @Bean
  @StepScope
  public Tasklet zipTasklet(
          @Value("#{jobParameters[acquirerFilename]}") String acquirerFilename,
          @Value("#{jobParameters[zipFilename]}") String acquirerZipFilename
  ) {
    return new ZipTasklet(acquirerFilename, acquirerZipFilename);
  }

  @Bean
  @StepScope
  public Tasklet saveAcquirerFileTasklet(
          AcquirerFileRepository acquirerFileRepository,
          @Value("#{jobParameters[zipFilename]}") String acquirerZipFilename
  ) {
    return new SaveAcquirerFileTasklet(acquirerZipFilename, acquirerFileRepository);
  }

  @Bean
  @StepScope
  public KeyPaginatedMongoReader<CardEntity> mongoItemReader(MongoTemplate mongoTemplate) {
    final var query = new Query();
    query.fields().include("hashPan", "hashPanChildren", "par", "exportConfirmed");
    return new KeyPaginatedMongoReaderBuilder<CardEntity>()
            .setMongoTemplate(mongoTemplate)
            .setCollectionName(COLLECTION_NAME)
            .setType(CardEntity.class)
            .setKeyName("hashPan")
            .setSortDirection(Sort.Direction.ASC)
            .setQuery(query)
            .setPageSize(1000)
            .build();
  }

  @Bean
  public ItemProcessor<CardEntity, List<String>> cardFlatProcessor() {
    return item -> {
      final var hashes = Optional.ofNullable(item.getHashPanChildren())
              .map(ArrayList::new)
              .orElse(new ArrayList<>());
      hashes.add(item.getHashPan());
      return hashes;
    };
  }

  @Bean
  @StepScope
  public SynchronizedItemStreamWriter<List<String>> acquirerFileWriter(
          @Value("#{jobParameters[acquirerFilename]}") String acquirerFilename
  ) throws Exception {
    final var fileWriter = new FlatFileItemWriter<List<String>>();
    fileWriter.setResource(new FileSystemResource(acquirerFilename));
    fileWriter.setAppendAllowed(false);
    fileWriter.setShouldDeleteIfExists(true);
    fileWriter.setForceSync(true);
    fileWriter.setLineAggregator(item -> Strings.join(item, '\n'));
    fileWriter.afterPropertiesSet();
    return new SynchronizedItemStreamWriterBuilder<List<String>>()
            .delegate(fileWriter)
            .build();
  }
}
