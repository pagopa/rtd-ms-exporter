package it.pagopa.gov.rtdmsexporter.application;

import io.reactivex.rxjava3.core.Flowable;
import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.application.acquirer.SaveAcquirerFileStep;
import it.pagopa.gov.rtdmsexporter.application.acquirer.ZipStep;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExportJob {

  private final PagedDatabaseExportStep exportDatabaseStep;
  private final ZipStep zipStep;
  private final SaveAcquirerFileStep acquirerFileStep;

  public ExportJob(PagedDatabaseExportStep exportDatabaseStep, ZipStep zipStep, SaveAcquirerFileStep acquirerFileStep) {
    this.exportDatabaseStep = exportDatabaseStep;
    this.zipStep = zipStep;
    this.acquirerFileStep = acquirerFileStep;
  }

  public Try<Boolean> run() {
    return Try.of(() -> Flowable.just(exportDatabaseStep.execute())
            .takeWhile(it -> it)
            .doOnNext(it -> log.info("Zipping"))
            .map(it -> zipStep.execute())
            .takeWhile(it -> it)
            .doOnNext(it -> log.info("Uploading"))
            .map(it -> acquirerFileStep.execute())
            .takeWhile(it -> it)
            .count()
            .blockingGet() == 1
    );
  }
}
