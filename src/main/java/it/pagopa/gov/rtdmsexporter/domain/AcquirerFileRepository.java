package it.pagopa.gov.rtdmsexporter.domain;

import java.util.Optional;

public interface AcquirerFileRepository {
  Optional<AcquirerFile> getAcquirerFile();
  boolean save(AcquirerFile file);
}
