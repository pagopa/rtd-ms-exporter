package it.pagopa.gov.rtdmsexporter.ports.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("batch")
public interface BatchRestController {
  @PutMapping("/start")
  ResponseEntity<String> startBatch();
}
