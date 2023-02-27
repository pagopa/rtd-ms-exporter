package it.pagopa.gov.rtdmsexporter.infrastructure.step;

import it.pagopa.gov.rtdmsexporter.infrastructure.ZipUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

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
        log.info("Original file to zip deleted {}", toZip);
        return true;
      } else {
        log.error("Failed generate zip file {}", zipFilename);
      }
    }
    log.error("File to zip not found {}", fileToZip);
    return false;
  }
}
