package it.pagopa.gov.rtdmsexporter.application;

import io.reactivex.rxjava3.core.Flowable;
import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.application.acquirer.SaveAcquirerFileStep;
import it.pagopa.gov.rtdmsexporter.application.acquirer.ZipStep;
import it.pagopa.gov.rtdmsexporter.application.paymentinstrument.NewExportedNotifyStep;
import lombok.extern.slf4j.Slf4j ;
import org.springframework.util.StopWatch;

@Slf4j
public class ExportJobService {

  private final PagedDatabaseExportStep exportDatabaseStep;
  private final ZipStep zipStep;
  private final SaveAcquirerFileStep acquirerFileStep;
  private final NewExportedNotifyStep exportedNotifyStep;

  public ExportJobService(
          PagedDatabaseExportStep exportDatabaseStep,
          ZipStep zipStep,
          SaveAcquirerFileStep acquirerFileStep,
          NewExportedNotifyStep exportedNotifyStep
  ) {
    this.exportDatabaseStep = exportDatabaseStep;
    this.zipStep = zipStep;
    this.acquirerFileStep = acquirerFileStep;
    this.exportedNotifyStep = exportedNotifyStep;
  }

  public Try<Boolean> execute() {
    log.info("Export job started");
    final var stopWatch = new StopWatch();
    stopWatch.start();
    final var completed = Try.of(() -> Flowable.just(exportDatabaseStep.execute())
            .takeWhile(it -> it)
            .doOnNext(it -> log.info("Zipping"))
            .map(it -> zipStep.execute())
            .takeWhile(it -> it)
            .doOnNext(it -> log.info("Uploading"))
            .map(it -> acquirerFileStep.execute())
            .takeWhile(it -> it)
            .doOnNext(it -> log.info("Publishing events"))
            .map(it -> exportedNotifyStep.execute())
            .takeWhile(it -> it)
            .count()
            .blockingGet() == 1);
    stopWatch.stop();
    log.info("Export job ends in {} ms", stopWatch.getTotalTimeMillis());
    return completed;
  }
}
