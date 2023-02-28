package it.pagopa.gov.rtdmsexporter.domain;

import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;

import java.util.List;

public interface PagedCardReader {
  List<CardEntity> read();
  void reset();
}