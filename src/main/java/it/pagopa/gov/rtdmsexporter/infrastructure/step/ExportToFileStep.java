package it.pagopa.gov.rtdmsexporter.infrastructure.step;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.domain.ChunkWriter;
import it.pagopa.gov.rtdmsexporter.domain.ExportDatabaseStep;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExportToFileStep implements ExportDatabaseStep {

  private final Flowable<List<CardEntity>> source;
  private final Function<CardEntity, List<String>> processElement;
  private final ChunkWriter<String> chunkWriter;
  private final Scheduler scheduler;
  private final int bufferBeforeWrite;

  public ExportToFileStep(
          Flowable<List<CardEntity>> source,
          Function<CardEntity, List<String>> processElement,
          ChunkWriter<String> chunkWriter,
          int bufferBeforeWrite,
          int parallelism
  ) {
    this.source = source;
    this.processElement = processElement;
    this.chunkWriter = chunkWriter;
    this.bufferBeforeWrite = bufferBeforeWrite;
    this.scheduler = Schedulers.from(Executors.newFixedThreadPool(parallelism));
  }

  @Override
  public Try<Long> execute() {
    return Try.of(() -> source.subscribeOn(scheduler)
            .flatMap(this::processToFlowable)
            .observeOn(scheduler)
            .buffer(bufferBeforeWrite)
            .map(chunkWriter::writeChunk)
            .takeWhile(Try::isSuccess)
            .collect(Collectors.summingLong(it -> it.getOrElse(0L)))
            .doOnSubscribe(disposable -> chunkWriter.open())
            .doOnTerminate(chunkWriter::close)
            .blockingGet()
    );
  }

  private Flowable<String> processToFlowable(List<CardEntity> cards) {
    return Flowable.fromIterable(cards)
            .subscribeOn(scheduler)
            .flatMap(card -> Flowable.fromIterable(processElement.apply(card)));
  }
}
