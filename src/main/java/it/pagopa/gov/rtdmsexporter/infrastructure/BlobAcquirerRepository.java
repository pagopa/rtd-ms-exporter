package it.pagopa.gov.rtdmsexporter.infrastructure;

import it.pagopa.gov.rtdmsexporter.domain.AcquirerFile;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class BlobAcquirerRepository implements AcquirerFileRepository {

  private static final String API_KEY_HEADER = "Ocp-Apim-Subscription-Key";

  private final String targetFilePath;
  private final String baseUrl;
  private final String filename;
  private final String apiKey;
  private final CloseableHttpClient httpClient;

  public BlobAcquirerRepository(String targetFilePath, String baseUrl, String filename, String apiKey, CloseableHttpClient httpClient) {
    this.targetFilePath = targetFilePath;
    this.baseUrl = baseUrl;
    this.filename = filename;
    this.apiKey = apiKey;
    this.httpClient = httpClient;
  }

  @Override
  public Optional<AcquirerFile> getAcquirerFile() {
    final var target = new File(targetFilePath);
    if (target.exists()) {
      return Optional.of(new AcquirerFile(target));
    } else {
      final var url = baseUrl.endsWith("/") ? baseUrl + filename : baseUrl + "/" + filename;
      final var getFile = new HttpGet(url);
      getFile.setHeader(API_KEY_HEADER, this.apiKey);
      log.info("Start download acquirer file");
      try {
        final var fileOrEmpty = httpClient.execute(getFile, saveLocally(target));
        log.info("Download successfully acquirer file");
        return fileOrEmpty.map(AcquirerFile::new);
      } catch (IOException ex) {
        log.error("Failed to download acquirer file ", ex);
      }
      return Optional.empty();
    }
  }

  @Override
  public boolean save(AcquirerFile file) {
    final var url = baseUrl.endsWith("/") ? baseUrl + filename : baseUrl + "/" + filename;
    final var toUpload = new FileEntity(
            file.file(),
            ContentType.create("application/octet-stream")
    );
    final var putFile = new HttpPut(url);
    putFile.setHeader(new BasicHeader("x-ms-blob-type", "BlockBlob"));
    putFile.setHeader(new BasicHeader("x-ms-version", "2021-04-10"));
    putFile.setEntity(toUpload);
    try (CloseableHttpResponse result = httpClient.execute(putFile)) {
      if (result.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
        log.info("Upload acquirer file successfully");
        return true;
      } else {
        log.error("Upload acquirer file failed {}", result.getStatusLine().getReasonPhrase());
        return false;
      }
    } catch (IOException e) {
      log.error("Upload acquirer file thrown exception {}", e.getMessage());
    }
    return false;
  }

  private ResponseHandler<Optional<File>> saveLocally(File target) {
    return response -> {
      try (var outputStream = new FileOutputStream(target)) {
        switch (response.getStatusLine().getStatusCode()) {
          case HttpStatus.SC_OK: {
            StreamUtils.copy(response.getEntity().getContent(), outputStream);
            return Optional.of(target);
          }
          case HttpStatus.SC_NOT_FOUND:
            return Optional.empty();
          default:
            throw new ClientProtocolException(response.getStatusLine().getReasonPhrase());
        }
      }
    };
  }
}
