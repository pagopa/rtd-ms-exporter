package it.pagopa.gov.rtdmsexporter.ports.rest;


import it.pagopa.gov.rtdmsexporter.batch.ExportJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RestController
@Slf4j
public class BatchRestControllerImpl implements BatchRestController {

  private final Executor singleThreadExecutor = Executors.newSingleThreadExecutor();
  private final ExportJobService exportJobService;

  @Autowired
  public BatchRestControllerImpl(ExportJobService exportJobService) {
    this.exportJobService = exportJobService;
  }

  @Override
  public ResponseEntity<String> startBatch() {
    singleThreadExecutor.execute(() -> {
      try {
        exportJobService.execute();
      } catch (Exception e) {
        log.error("Error during job execution {}", e.getMessage());
      }
    });
    return ResponseEntity.ok("");
  }
}
