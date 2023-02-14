package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.domain.CardExport;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ExportJobConfiguration {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;

  public ExportJobConfiguration(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
    this.jobRepository = jobRepository;
    this.platformTransactionManager = platformTransactionManager;
  }

  @Bean
  Job exportJob(
          Step downloadAcquirerFileStep,
          Step loadIndexesStep
  ) {
    return new JobBuilder("exportJob", jobRepository)
            .preventRestart()
            .incrementer(new RunIdIncrementer())
            .start(downloadAcquirerFileStep)
            .next(loadIndexesStep)
            .build();
  }

  @Bean
  Step downloadAcquirerFileStep(AcquirerFileRepository acquirerFileRepository) {
    return new StepBuilder("downloadAcquirerFileStep", jobRepository)
            .tasklet(new DownloadAcquirerFile(acquirerFileRepository), platformTransactionManager)
            .build();
  }

  @Bean
  @JobScope
  Step loadIndexesStep(
          CardExport cardExport,
          @Value("#{jobParameters['fileToIndex']}") String acquirerFile,
          @Value("#{jobParameters['chunks']}") int chunks
  ) {
    return new StepBuilder("loadIndexesStep", jobRepository)
            .<String, String>chunk(chunks, platformTransactionManager)
            .reader(acquirerFileReader(acquirerFile))
            .writer(cardExportIndexer(cardExport))
            .build();
  }

  ItemReader<String> acquirerFileReader(String acquirerFile) {
    final var itemReader = new FlatFileItemReader<String>();
    itemReader.setLinesToSkip(0);
    itemReader.setLineMapper((line, lineNumber) -> line);
    itemReader.setResource(new FileSystemResource(acquirerFile));
    return itemReader;
  }

  ItemWriter<String> cardExportIndexer(CardExport cardExport) {
    return new CardExportIndexer(cardExport);
  }

}
