package it.pagopa.gov.rtdmsexporter.application.paymentinstrument;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardPublisher;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NewExportedNotifyStep {

  private final ExportedCardRepository exportedCardRepository;
  private final ExportedCardPublisher exportedCardPublisher;

  public NewExportedNotifyStep(ExportedCardRepository exportedCardRepository, ExportedCardPublisher exportedCardPublisher) {
    this.exportedCardRepository = exportedCardRepository;
    this.exportedCardPublisher = exportedCardPublisher;
  }

  public boolean execute() {
    Flowable.fromStream(exportedCardRepository.exportedPaymentInstruments())
            .observeOn(Schedulers.single())
            .map(exportedCardPublisher::notifyExportedCard)
            .doOnEach(item -> log.info("Send export event {}", item))
            .filter(Try::isSuccess)
            .doOnNext(item -> exportedCardRepository.remove(item.get()))
            .blockingSubscribe();
    return true;
  }
}
