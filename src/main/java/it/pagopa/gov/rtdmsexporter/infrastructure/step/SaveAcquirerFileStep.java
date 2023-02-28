package it.pagopa.gov.rtdmsexporter.infrastructure.step;

import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFile;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public class SaveAcquirerFileStep {

  private final String fileToUpload;
  private final AcquirerFileRepository acquirerFileRepository;

  public SaveAcquirerFileStep(String fileToUpload, AcquirerFileRepository acquirerFileRepository) {
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
    log.error("File to save doesn't exists {}", fileToUpload);
    return false;
  }
}
