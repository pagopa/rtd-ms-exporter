package it.pagopa.gov.rtdmsexporter.application;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.pagopa.gov.rtdmsexporter.batch.ExportJob;
import it.pagopa.gov.rtdmsexporter.batch.ChunkBufferedWriter;
import it.pagopa.gov.rtdmsexporter.batch.tasklet.ZipTasklet;
import it.pagopa.gov.rtdmsexporter.domain.CardProcessor;
import it.pagopa.gov.rtdmsexporter.domain.CardReader;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RxCardExportJob implements ExportJob {

  private final CardReader<CardEntity> cardReader;
  private final CardProcessor cardProcessor;
  private final ChunkBufferedWriter bufferedWriter;
  private final ZipTasklet zipTasklet;
  private final Executor executor;
  private final Scheduler scheduler;
  private final CountDownLatch countDownLatch;

  public RxCardExportJob(
          CardReader<CardEntity> cardReader,
          CardProcessor cardProcessor,
          ChunkBufferedWriter bufferedWriter,
          ZipTasklet zipTasklet,
          int threadPool
  ) {
    this.cardReader = cardReader;
    this.cardProcessor = cardProcessor;
    this.bufferedWriter = bufferedWriter;
    this.zipTasklet = zipTasklet;
    this.executor = Executors.newFixedThreadPool(threadPool);
    this.scheduler = Schedulers.from(executor);
    this.countDownLatch = new CountDownLatch(1);
  }


  @SneakyThrows
  public boolean start() {
    source().subscribeOn(scheduler)
            .flatMap(it -> Flowable
                    .fromStream(it)
                    .subscribeOn(scheduler)
                    .flatMap(this::processBatch)
            )
            .buffer(50_000)
            .observeOn(scheduler)
            .doFinally(countDownLatch::countDown)
            .subscribe(bufferedWriter::write);
    countDownLatch.await();
    return true;
  }

  private Flowable<Stream<CardEntity>> source() {
    return Flowable.<Stream<CardEntity>>generate(emitter -> {
      final var batch = cardReader.read();
      if (Objects.nonNull(batch) && batch.hasNext()) {
        emitter.onNext(
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(batch, Spliterator.IMMUTABLE), false)
        );
      } else {
        emitter.onComplete();
      }
    }).onBackpressureBuffer();
  }

  private Flowable<String> processBatch(CardEntity batch) {
    return Flowable.just(batch)
            .flatMap(card  -> Flowable.fromIterable(cardProcessor.apply(card)));
  }
}
