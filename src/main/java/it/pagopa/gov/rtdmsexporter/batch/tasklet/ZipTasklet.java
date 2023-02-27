package it.pagopa.gov.rtdmsexporter.batch.tasklet;

import com.github.tonivade.purefun.type.Try;
import it.pagopa.gov.rtdmsexporter.infrastructure.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public class ZipTasklet implements Tasklet {

  private final String fileToZip;
  private final String zipFilename;

  public ZipTasklet(String fileToZip, String zipFilename) {
    this.fileToZip = fileToZip;
    this.zipFilename = zipFilename;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    final var toZip = new File(fileToZip);
    if (toZip.exists()) {
      final var acquirerFile = ZipUtils.zipFile(toZip, zipFilename);
      if (acquirerFile.isPresent()) {
        contribution.setExitStatus(ExitStatus.COMPLETED);
        Try.of(() -> { Files.delete(toZip.toPath()); return true; })
                .onFailure(error -> log.warn("Failed to delete zipped file {}, cause {}", toZip.getPath(), error))
                .onSuccess(it -> log.info("Original file {} to zip deleted", toZip));
        return RepeatStatus.FINISHED;
      }
    }
    contribution.setExitStatus(ExitStatus.FAILED);
    return null;
  }
}
