package it.pagopa.gov.rtdmsexporter.infrastructure;

import it.pagopa.gov.rtdmsexporter.domain.CardExport;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class SetCardExport implements CardExport {

  private final Set<String> cards;

  public SetCardExport() {
    this.cards = new HashSet<>();
  }

  @Override
  public boolean add(String key, String... values) {
    return cards.add(key);
  }

  @Override
  public boolean remove(String key) {
    return cards.remove(key);
  }

  @Override
  public Stream<String> cards() {
    return cards.stream();
  }
}
