package it.pagopa.gov.rtdmsexporter.domain.paymentinstrument;

import io.vavr.control.Try;

public interface ExportedCardPublisher {
  Try<String> notifyExportedCard(String cardId);
}
