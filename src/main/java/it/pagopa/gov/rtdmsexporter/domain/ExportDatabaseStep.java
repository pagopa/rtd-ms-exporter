package it.pagopa.gov.rtdmsexporter.domain;

import com.github.tonivade.purefun.type.Try;

public interface ExportDatabaseStep {
  Try<Long> execute();
}
