package it.pagopa.gov.rtdmsexporter.domain.acquirer;

import io.vavr.control.Try;
import java.io.IOException;

public interface ChunkWriter<T> {
  void open() throws IOException;
  Try<Long> writeChunk(Iterable<? extends T> items);
  void close() throws IOException;
}
