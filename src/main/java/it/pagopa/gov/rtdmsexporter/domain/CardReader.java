package it.pagopa.gov.rtdmsexporter.domain;

import java.util.Iterator;

public interface CardReader<T> {
  Iterator<T> read();
}