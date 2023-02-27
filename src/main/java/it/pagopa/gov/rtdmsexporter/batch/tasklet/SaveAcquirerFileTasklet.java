package it.pagopa.gov.rtdmsexporter.batch.tasklet;

import com.github.tonivade.purefun.type.Try;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFile;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public class SaveAcquirerFileTasklet {

  private final String fileToUpload;
  private final AcquirerFileRepository acquirerFileRepository;

  public SaveAcquirerFileTasklet(String fileToUpload, AcquirerFileRepository acquirerFileRepository) {
    this.fileToUpload = fileToUpload;
    this.acquirerFileRepository = acquirerFileRepository;
  }

  public boolean execute() throws Exception {
    final var acquirerFile = new AcquirerFile(new File(fileToUpload));
    if (acquirerFile.file().exists()) {
      final var saved = acquirerFileRepository.save(acquirerFile);
      Try.of(() -> { Files.delete(acquirerFile.file().toPath()); return true; })
              .onFailure(error -> log.warn("Failed to delete uploaded file {}, cause {}", acquirerFile.file().getPath(), error));
      return saved;
    }
    log.info("File to save doesn't exists {}", fileToUpload);
    return false;
  }
}
