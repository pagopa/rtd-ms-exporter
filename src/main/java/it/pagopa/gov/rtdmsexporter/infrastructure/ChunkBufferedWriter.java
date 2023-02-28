package it.pagopa.gov.rtdmsexporter.infrastructure;

import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.domain.ChunkWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

@Slf4j
public class ChunkBufferedWriter implements ChunkWriter<String> {
  private final File file;
  private BufferedWriter writer;

  public ChunkBufferedWriter(File file) {
    this.file = file;
  }

  @Override
  public void open() throws IOException {
    if (Objects.isNull(writer)) {
      Files.deleteIfExists(file.toPath());
      Files.createFile(file.toPath());
      writer = new BufferedWriter(new FileWriter(file, false));
    }
    log.info("File writer has been open");
  }

  @Override
  public Try<Long> writeChunk(Iterable<? extends String> items) {
    try {
      var wrote = 0L;
      synchronized (this) {
        for (var item : items) {
          writer.write(item);
          writer.newLine();
          wrote += 1;
        }
      }
      return Try.success(wrote);
    } catch (IOException ex) {
      return Try.failure(ex);
    }
  }

  @Override
  public void close() throws IOException {
    if (Objects.nonNull(writer)) {
      writer.close();
    }
    log.info("File writer has been closed");
  }
}