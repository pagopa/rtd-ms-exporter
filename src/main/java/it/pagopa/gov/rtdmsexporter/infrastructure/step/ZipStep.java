package it.pagopa.gov.rtdmsexporter.infrastructure.step;

import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.infrastructure.ZipUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public class ZipStep {

  private final String fileToZip;
  private final String zipFilename;

  public ZipStep(String fileToZip, String zipFilename) {
    this.fileToZip = fileToZip;
    this.zipFilename = zipFilename;
  }

  public boolean execute() throws Exception {
    final var toZip = new File(fileToZip);
    if (toZip.exists()) {
      final var acquirerFile = ZipUtils.zipFile(toZip, zipFilename);
      if (acquirerFile.isPresent()) {
        Try.of(() -> { Files.delete(toZip.toPath()); return true; })
                .onFailure(error -> log.warn("Failed to delete zipped file {}, cause {}", toZip.getPath(), error))
                .onSuccess(it -> log.info("Original file {} to zip deleted", toZip));
        return true;
      } else {
        log.error("Failed generate zip file {}", zipFilename);
      }
    } else {
      log.error("File to zip not found {}", fileToZip);
    }
    return false;
  }
}
