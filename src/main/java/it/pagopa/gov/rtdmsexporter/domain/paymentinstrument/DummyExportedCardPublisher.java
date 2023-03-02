package it.pagopa.gov.rtdmsexporter.domain.paymentinstrument;

import io.vavr.control.Try;

public class DummyExportedCardPublisher implements ExportedCardPublisher {
  @Override
  public Try<String> notifyExportedCard(String cardId) {
    return Try.success(cardId);
  }
}
