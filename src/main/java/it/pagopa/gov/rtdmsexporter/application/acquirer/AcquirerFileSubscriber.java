package it.pagopa.gov.rtdmsexporter.application.acquirer;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.domain.acquirer.ChunkWriter;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A flowable subscriber which collect payment instrument to acquirer file
 */
@Slf4j
public class AcquirerFileSubscriber implements Function<Flowable<List<CardEntity>>, Future<Long>> {

  private final Function<CardEntity, List<String>> processElement;
  private final ChunkWriter<String> chunkWriter;
  private final Scheduler scheduler;
  private final int bufferBeforeWrite;

  public AcquirerFileSubscriber(Function<CardEntity, List<String>> processElement, ChunkWriter<String> chunkWriter, Scheduler scheduler, int bufferBeforeWrite) {
    this.processElement = processElement;
    this.chunkWriter = chunkWriter;
    this.scheduler = scheduler;
    this.bufferBeforeWrite = bufferBeforeWrite;
  }

  @Override
  public Future<Long> apply(Flowable<List<CardEntity>> source) {
    final var stopWatch = new StopWatch();
    return Flowable.fromPublisher(source)
            .observeOn(scheduler)
            .flatMap(this::processToFlowable)
            .buffer(bufferBeforeWrite)
            .map(chunkWriter::writeChunk)
            .takeWhile(Try::isSuccess)
            .collect(Collectors.summingLong(it -> it.getOrElse(0L)))
            .doOnTerminate(chunkWriter::close)
            .doOnSubscribe(disposable -> {
              stopWatch.start();
              chunkWriter.open();
            })
            .toCompletionStage()
            .toCompletableFuture()
            .whenComplete((count, error) -> {
              stopWatch.stop();
              if (Objects.isNull(error)) {
                log.info("Read and write to file {} in {} ms", count, stopWatch.getTotalTimeMillis());
              } else {
                log.error("Error during write to acquirer file", error);
              }
            });
  }

  private Flowable<String> processToFlowable(List<CardEntity> cards) {
    return Flowable.fromIterable(cards)
            .subscribeOn(scheduler)
            .flatMap(card -> Flowable.fromIterable(processElement.apply(card)));
  }
}
