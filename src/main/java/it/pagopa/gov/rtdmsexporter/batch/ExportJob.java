package it.pagopa.gov.rtdmsexporter.batch;


import org.springframework.batch.core.JobExecution;

public interface ExportJob {
  JobExecution execute();
}
