package it.pagopa.gov.rtdmsexporter.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.util.StopWatch;

import java.util.Date;

@Slf4j
public class ExportJobService {

  public static final String TARGET_ACQUIRER_FILENAME_KEY = "acquirerFilename";
  public static final String TARGET_ACQUIRER_ZIP_KEY = "zipFilename";

  private final ExportJob exportJob;

  private final String acquirerTargetFile;
  private final String acquirerZipTargetFile;

  public ExportJobService(ExportJob exportJob, String acquirerTargetFile, String acquirerZipTargetFile) {
    this.exportJob = exportJob;
    this.acquirerTargetFile = acquirerTargetFile;
    this.acquirerZipTargetFile = acquirerZipTargetFile;
  }

  public boolean execute() throws Exception {
    log.info("Export job started");
    final var stopWatch = new StopWatch();
    final var parameters = new JobParametersBuilder()
            .addLong("timestamp", new Date().getTime())
            .addString(TARGET_ACQUIRER_FILENAME_KEY, acquirerTargetFile)
            .addString(TARGET_ACQUIRER_ZIP_KEY, acquirerZipTargetFile)
            .toJobParameters();
    stopWatch.start();
    final var completed = exportJob.start();
    stopWatch.stop();
    log.info("Export job ends at {}", stopWatch.getTotalTimeMillis());
    return completed;
  }

}
