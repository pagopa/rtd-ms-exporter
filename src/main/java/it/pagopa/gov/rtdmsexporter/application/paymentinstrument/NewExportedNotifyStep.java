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
    final var successes = Flowable.fromStream(exportedCardRepository.exportedPaymentInstruments())
            .observeOn(Schedulers.single())
            .map(exportedCardPublisher::notifyExportedCard)
            .doOnNext(this::logIfError)
            .filter(Try::isSuccess)
            .doOnNext(item -> exportedCardRepository.remove(item.get()))
            .count()
            .blockingGet();
    log.info("Successfully published {} events", successes);
    return true;
  }

  private void logIfError(Try<?> result) {
    result.onFailure(it -> log.error("Failed to publish event", it));
  }
}
