package it.pagopa.gov.rtdmsexporter.infrastructure.paymentinstrument;

import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MemoryExportedCardRepository implements ExportedCardRepository {

  private final Set<String> exportedInstruments;

  public MemoryExportedCardRepository() {
    exportedInstruments = ConcurrentHashMap.newKeySet();
  }

  @Override
  public void save(String paymentInstrumentId) {
    exportedInstruments.add(paymentInstrumentId);
  }

  @Override
  public void remove(String paymentInstrumentId) {
    exportedInstruments.remove(paymentInstrumentId);
  }

  @Override
  public Stream<String> exportedPaymentInstruments() {
    // return a safe copy
    return new HashSet<>(exportedInstruments).stream();
  }
}
