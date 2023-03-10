package it.pagopa.gov.rtdmsexporter.application.paymentinstrument;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * A subscriber which collect the new exported payment instrument starting from a flowable
 */
@Slf4j
public class NewExportedSubscriber implements Function<Flowable<List<CardEntity>>, Future<Long>> {

  private final Scheduler scheduler;
  private final ExportedCardRepository exportedCardRepository;

  public NewExportedSubscriber(Scheduler scheduler, ExportedCardRepository exportedCardRepository) {
    this.scheduler = scheduler;
    this.exportedCardRepository = exportedCardRepository;
  }

  @Override
  public Future<Long> apply(Flowable<List<CardEntity>> source) {
    return Flowable.fromPublisher(source)
            .observeOn(scheduler)
            .flatMap(Flowable::fromIterable)
            .filter(it -> !it.isExported())
            .collectInto(exportedCardRepository, (repo, item) -> repo.save(item.getHashPan()))
            .map(it -> it.exportedPaymentInstruments().count())
            .doOnSuccess(count -> log.info("Collected {} new exported card", count))
            .toFuture();
  }
}
