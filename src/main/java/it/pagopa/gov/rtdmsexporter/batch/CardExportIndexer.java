package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.domain.CardExport;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class CardExportIndexer implements ItemWriter<String> {

  private final CardExport cardExport;

  public CardExportIndexer(CardExport cardExport) {
    this.cardExport = cardExport;
  }

  @Override
  public void write(Chunk<? extends String> chunk) throws Exception {
    chunk.forEach(cardExport::add);
  }
}
