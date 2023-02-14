package it.pagopa.gov.rtdmsexporter.domain;

public interface Exporter {

  boolean add(String key, String value);
  boolean add(String key, String... values);
  boolean remove(String key);
}
