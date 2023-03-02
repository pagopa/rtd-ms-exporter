package it.pagopa.gov.rtdmsexporter.domain.paymentinstrument;

public interface ExportedCardPublisher {
  void notifyExportedCard(String cardId);
}
