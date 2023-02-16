package it.pagopa.gov.rtdmsexporter.infrastructure;

import it.pagopa.gov.rtdmsexporter.domain.AcquirerFile;
import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;

import java.io.IOException;

@Slf4j
public class BlobAcquirerRepository implements AcquirerFileRepository {

  private static final String API_KEY_HEADER = "Ocp-Apim-Subscription-Key";

  private final String baseUrl;
  private final String remoteFilename;
  private final String apiKey;
  private final CloseableHttpClient httpClient;

  public BlobAcquirerRepository(String baseUrl, String remoteFilename, String apiKey, CloseableHttpClient httpClient) {
    this.baseUrl = baseUrl;
    this.remoteFilename = remoteFilename;
    this.apiKey = apiKey;
    this.httpClient = httpClient;
  }

  @Override
  public boolean save(AcquirerFile file) {
    final var url = baseUrl.endsWith("/") ? baseUrl + remoteFilename : baseUrl + "/" + remoteFilename;
    final var toUpload = new FileEntity(
            file.file(),
            ContentType.create("application/octet-stream")
    );
    final var putFile = new HttpPut(url);
    putFile.setHeader(new BasicHeader(API_KEY_HEADER, apiKey));
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
}
