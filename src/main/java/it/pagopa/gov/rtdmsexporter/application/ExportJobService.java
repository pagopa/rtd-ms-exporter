package it.pagopa.gov.rtdmsexporter.application;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

@Slf4j
public class ExportJobService {

  private final ExportJob exportJob;

  public ExportJobService(ExportJob exportJob) {
    this.exportJob = exportJob;
  }

  public Try<Boolean> execute() throws Exception {
    log.info("Export job started");
    final var stopWatch = new StopWatch();
    stopWatch.start();
    final var completed = exportJob.run();
    stopWatch.stop();
    log.info("Export job ends at {}", stopWatch.getTotalTimeMillis());
    return completed;
  }

}
