package it.pagopa.gov.rtdmsexporter.application;

import io.reactivex.rxjava3.core.Flowable;
import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.domain.ExportDatabaseStep;
import it.pagopa.gov.rtdmsexporter.infrastructure.step.SaveAcquirerFileStep;
import it.pagopa.gov.rtdmsexporter.infrastructure.step.ZipStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

@Slf4j
public class ExportJob {

  private final ExportDatabaseStep exportDatabaseStep;
  private final ZipStep zipStep;
  private final SaveAcquirerFileStep acquirerFileStep;

  public ExportJob(ExportDatabaseStep exportDatabaseStep, ZipStep zipStep, SaveAcquirerFileStep acquirerFileStep) {
    this.exportDatabaseStep = exportDatabaseStep;
    this.zipStep = zipStep;
    this.acquirerFileStep = acquirerFileStep;
  }

  public Try<Boolean> run() {
    final var stopWatch = new StopWatch();
    stopWatch.start();
    return Try.of(() -> Flowable.just(exportDatabaseStep.execute())
            .flatMap(it -> it.fold(Flowable::error, Flowable::just))
            .doOnEach(it -> {
              stopWatch.stop();
              log.info("Export {} records in {} ms, zipping it", it.getValue(), stopWatch.getTotalTimeMillis());
            })
            .map(it -> zipStep.execute())
            .takeWhile(it -> it)
            .map(it -> acquirerFileStep.execute())
            .takeWhile(it -> it)
            .count()
            .blockingGet() == 1
    );
  }
}
