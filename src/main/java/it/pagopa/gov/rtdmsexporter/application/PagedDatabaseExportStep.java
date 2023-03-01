package it.pagopa.gov.rtdmsexporter.application;

import io.reactivex.rxjava3.core.*;
import it.pagopa.gov.rtdmsexporter.domain.PagedCardReader;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * An abstraction over a step which export from a flowable to multiple subscribers.
 * It handles a hot flowable.
 */
@Slf4j
public class PagedDatabaseExportStep {

  private final Scheduler scheduler;
  private final PagedCardReader pagedCardReader;
  private final List<Function<Flowable<List<CardEntity>>, Future<?>>> subscribers;

  public PagedDatabaseExportStep(
          Scheduler scheduler,
          PagedCardReader pagedCardReader,
          Function<Flowable<List<CardEntity>>, Future<?>>... subscribers
  ) {
    this.scheduler = scheduler;
    this.pagedCardReader = pagedCardReader;
    this.subscribers = Arrays.stream(subscribers).toList();
  }

  public Boolean execute() {
    // create hot flowable
    final var hotSource = getFlowableSource().subscribeOn(scheduler).publish();
    final var flowableSubscribers = subscribers.stream()
            .map(subscriber -> subscriber.apply(hotSource))
            .map(Flowable::fromFuture)
            .toList();
    hotSource.connect();
    Flowable.merge(flowableSubscribers).blockingSubscribe();
    return true;
  }

  private Flowable<List<CardEntity>> getFlowableSource() {
    return Flowable.<List<CardEntity>>generate(emitter -> {
      final var batch = pagedCardReader.read();
      if (Objects.nonNull(batch) && !batch.isEmpty()) {
        emitter.onNext(batch);
      } else {
        emitter.onComplete();
      }
    }).onBackpressureBuffer().doFinally(pagedCardReader::reset);
  }
}
