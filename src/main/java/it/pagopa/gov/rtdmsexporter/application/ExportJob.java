package it.pagopa.gov.rtdmsexporter.application;

import com.github.tonivade.purefun.type.Try;
import io.reactivex.rxjava3.core.Flowable;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFile;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import it.pagopa.gov.rtdmsexporter.domain.ExportDatabaseStep;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public class ExportJob {

  private final ExportDatabaseStep exportDatabaseStep;

  private final String targetFile;
  private final AcquirerFileRepository acquirerFileRepository;

  public ExportJob(ExportDatabaseStep exportDatabaseStep, AcquirerFileRepository acquirerFileRepository, String targetFile) {
    this.exportDatabaseStep = exportDatabaseStep;
    this.acquirerFileRepository = acquirerFileRepository;
    this.targetFile = targetFile;
  }

  Try<Boolean> run() {
    final var acquirerFile = new AcquirerFile(new File(targetFile));
    final var disposable = Flowable.just(exportDatabaseStep.execute())
            .flatMap(it -> it.fold(error -> Flowable.error(error.getCause()), Flowable::just))
            .doOnEach(it -> log.info("Exported {} records", it.getValue()))
            .map(it -> acquirerFileRepository.save(acquirerFile))
            //.doOnEach(it -> Files.deleteIfExists(acquirerFile.file().toPath()))
            .subscribe();
    return Try.success(true);
  }
}
