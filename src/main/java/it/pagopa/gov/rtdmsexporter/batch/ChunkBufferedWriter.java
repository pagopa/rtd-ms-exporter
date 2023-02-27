package it.pagopa.gov.rtdmsexporter.batch;

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

  public void write(List<String> items) throws IOException {
    synchronized (this) {
      for(var item : items) {
        writer.write(item);
        writer.newLine();
      }
    }
  }
}