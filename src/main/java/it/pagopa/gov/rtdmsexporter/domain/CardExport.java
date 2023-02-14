package it.pagopa.gov.rtdmsexporter.domain;

import java.util.stream.Stream;

public interface CardExport {

  boolean add(String key, String... values);
  boolean remove(String key);

  Stream<String> cards();
}
