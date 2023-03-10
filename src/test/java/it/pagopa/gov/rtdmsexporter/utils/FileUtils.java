package it.pagopa.gov.rtdmsexporter.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

public final class FileUtils {

  public static File generateFile(String path, Stream<String> lines) throws IOException {
    final var file = new File(path);
    generateFile(file, lines);
    return file;
  }

  public static void generateFile(File file, Stream<String> lines) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      lines.forEach(it -> {
        try {
          writer.write(it);
          writer.newLine();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }

}
