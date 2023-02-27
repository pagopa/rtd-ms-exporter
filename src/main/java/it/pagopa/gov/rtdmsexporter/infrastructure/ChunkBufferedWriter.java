package it.pagopa.gov.rtdmsexporter.infrastructure;

import com.github.tonivade.purefun.type.Try;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ChunkBufferedWriter {
  private final BufferedWriter writer;

  public ChunkBufferedWriter(File file) throws IOException {
    writer = new BufferedWriter(new FileWriter(file));
  }

  public Try<Integer> write(List<String> items) {
    try {
      synchronized (this) {
        for (var item : items) {
          writer.write(item);
          writer.newLine();
        }
      }
      return Try.success(items.size());
    } catch (IOException ex) {
      return Try.failure(ex);
    }
  }
}