package it.pagopa.gov.rtdmsexporter.infrastructure;

import it.pagopa.gov.rtdmsexporter.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ZipUtilsTest {

  private File toZip;

  @BeforeEach
  public void setup() throws IOException {
    toZip = new File("toZip.csv");
    FileUtils.generateFile(toZip, Stream.of("testo"));
  }

  @AfterEach
  public void clean() {
    toZip.delete();
  }

  @Test
  void shouldZipFileSuccessfully() throws IOException {
    final var zip = ZipUtils.zipFile(toZip, "./zipped.zip").get();
    assertThat(zip.exists()).isTrue();
    try (final var in = new ZipInputStream(new FileInputStream(zip))) {
      assertThat(in.getNextEntry()).isNotNull();
    }
    zip.delete();
  }

  @Test
  void whenZipFileExistThenIsOverwritten() throws IOException {
    ZipUtils.zipFile(toZip, "./zipped.zip");
    final var zip = ZipUtils.zipFile(toZip, "./zipped.zip").get();
    assertThat(zip.exists()).isTrue();
    try (final var in = new ZipInputStream(new FileInputStream(zip))) {
      assertThat(in.getNextEntry()).isNotNull();
    }
    zip.delete();
  }

}