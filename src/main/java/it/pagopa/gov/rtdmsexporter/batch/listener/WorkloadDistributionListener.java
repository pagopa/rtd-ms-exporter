package it.pagopa.gov.rtdmsexporter.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterRead;
import org.springframework.batch.core.annotation.AfterStep;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WorkloadDistributionListener<T> {

  private final Map<String, Integer> workload;

  public WorkloadDistributionListener() {
    this.workload = new ConcurrentHashMap<>();
  }

  @AfterRead
  void afterRead(T item) {
    workload.compute(Thread.currentThread().getName(), (key, value) -> Objects.isNull(value) ? 1 : value + 1);
  }

  @AfterStep
  public ExitStatus afterStep(StepExecution stepExecution) {
    log.info("Workload info");
    workload.forEach((key, value) -> log.info("{} - {}", key, value));
    return null;
  }
}
