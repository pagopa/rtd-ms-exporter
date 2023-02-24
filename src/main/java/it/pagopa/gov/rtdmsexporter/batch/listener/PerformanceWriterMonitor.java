package it.pagopa.gov.rtdmsexporter.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PerformanceWriterMonitor<T> implements ItemWriteListener<T> {

  private Map<String, Long> startTimes = new ConcurrentHashMap<>();

  @Override
  public void beforeWrite(Chunk<? extends T> items) {
    startTimes.compute(
            Thread.currentThread().getName(),
            (key, value) -> System.currentTimeMillis()
    );
  }

  @Override
  public void afterWrite(Chunk<? extends T> items) {
    log.info("Write chunk in {}ms", System.currentTimeMillis() - startTimes.get(Thread.currentThread().getName()));
  }
}
