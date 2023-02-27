package it.pagopa.gov.rtdmsexporter.batch.tasklet;

import it.pagopa.gov.rtdmsexporter.infrastructure.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;

@Slf4j
public class ZipTasklet {

  private final String fileToZip;
  private final String zipFilename;

  public ZipTasklet(String fileToZip, String zipFilename) {
    this.fileToZip = fileToZip;
    this.zipFilename = zipFilename;
  }

  public boolean execute() throws Exception {
    final var toZip = new File(fileToZip);
    if (toZip.exists()) {
      final var acquirerFile = ZipUtils.zipFile(toZip, zipFilename);
      if (acquirerFile.isPresent()) {
        log.info("Original file to zip deleted {}", toZip);
        return true;
      }
    }
    return false;
  }
}
