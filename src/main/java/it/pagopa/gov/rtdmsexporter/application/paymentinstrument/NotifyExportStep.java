package it.pagopa.gov.rtdmsexporter.application.paymentinstrument;

import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardPublisher;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardRepository;

public class NotifyExportStep {

  private final ExportedCardRepository exportPaymentInstrumentRepository;
  private final ExportedCardPublisher exportedCardPublisher;

  public NotifyExportStep(ExportedCardRepository exportPaymentInstrumentRepository, ExportedCardPublisher exportedCardPublisher) {
    this.exportPaymentInstrumentRepository = exportPaymentInstrumentRepository;
    this.exportedCardPublisher = exportedCardPublisher;
  }

  public Try<Long> execute() {
//    Flowable.fromStream(exportPaymentInstrumentRepository.exportedPaymentInstruments())
//            .parallel()
//            .map(this::notifySingleExport)
//            .collect(Collectors.counting());
    return Try.success(0l);
  }

  private Try<Void> notifySingleExport(String paymentInstrumentId) {

    return Try.success(null);
  }
}
