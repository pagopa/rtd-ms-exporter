package it.pagopa.gov.rtdmsexporter.domain;

import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class CardProcessor implements Function<CardEntity, List<String>> {
  @Override
  public List<String> apply(CardEntity cardEntity) {
    return Stream.concat(
            Stream.of(cardEntity.getHashPan()),
            cardEntity.getHashPanChildren().stream()
    ).toList();
  }
}
