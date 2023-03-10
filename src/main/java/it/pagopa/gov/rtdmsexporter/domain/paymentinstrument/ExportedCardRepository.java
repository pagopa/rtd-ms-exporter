package it.pagopa.gov.rtdmsexporter.domain.paymentinstrument;

import java.util.stream.Stream;

public interface ExportedCardRepository {
  void save(String paymentInstrumentId);
  void remove(String paymentInstrumentId);
  Stream<String> exportedPaymentInstruments();
}
