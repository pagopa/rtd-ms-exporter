package it.pagopa.gov.rtdmsexporter.infrastructure;

import it.pagopa.gov.rtdmsexporter.domain.AcquirerFile;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class LocalAcquirerFileRepository implements AcquirerFileRepository {

  private final String acquirerFile;

  public LocalAcquirerFileRepository(String acquirerFile) {
    this.acquirerFile = acquirerFile;
  }

  @Override
  public Future<AcquirerFile> getAcquirerFile() {
    return CompletableFuture.completedFuture(new AcquirerFile(new File(this.acquirerFile)));
  }

  @Override
  public Future<Void> save(AcquirerFile file) {
    return null;
  }
}
