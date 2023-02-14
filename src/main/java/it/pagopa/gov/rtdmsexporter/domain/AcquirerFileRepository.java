package it.pagopa.gov.rtdmsexporter.domain;

import java.util.concurrent.Future;

public interface AcquirerFileRepository {
  Future<AcquirerFile> getAcquirerFile();
  Future<Void> save(AcquirerFile file);
}
