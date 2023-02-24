package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.infrastructure.ZipUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;

public class ZipAcquirerTasklet implements Tasklet {

  private final String generatedAcquirerFile;
  private final String zipFilepath;

  public ZipAcquirerTasklet(String generatedAcquirerFile, String zipFilepath) {
    this.generatedAcquirerFile = generatedAcquirerFile;
    this.zipFilepath = zipFilepath;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    final var toZip = new File(generatedAcquirerFile);
    if (toZip.exists()) {
      final var zipped = ZipUtils.zipFile(toZip, zipFilepath);
      if (zipped.isPresent()) {

      }
    }

    return null;
  }
}
