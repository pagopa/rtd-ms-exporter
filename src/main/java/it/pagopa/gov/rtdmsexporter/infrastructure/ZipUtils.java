package it.pagopa.gov.rtdmsexporter.infrastructure;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {

  private ZipUtils() {}

  public static Optional<File> zipFile(File file, String zipFilePath) throws IOException {
    final var fileZip = new File(zipFilePath);
    if (!fileZip.exists() && !fileZip.createNewFile()) {
      throw new IOException("Failed to create zip file at " + zipFilePath);
    }
    try (final var in = new FileInputStream(file)) {
      try (final var out = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
        out.putNextEntry(new ZipEntry(file.getName()));
        IOUtils.copyLarge(in, out);
      }
    }

    return Optional.of(fileZip);
  }
}
