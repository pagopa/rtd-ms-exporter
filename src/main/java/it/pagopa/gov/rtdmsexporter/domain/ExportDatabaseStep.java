package it.pagopa.gov.rtdmsexporter.domain;

import io.vavr.control.Try;

public interface ExportDatabaseStep {
  Try<Long> execute();
}
