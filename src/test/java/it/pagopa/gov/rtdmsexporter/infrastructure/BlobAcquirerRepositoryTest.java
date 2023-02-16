package it.pagopa.gov.rtdmsexporter.infrastructure;

import it.pagopa.gov.rtdmsexporter.domain.AcquirerFile;
import it.pagopa.gov.rtdmsexporter.utils.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class BlobAcquirerRepositoryTest {

  private CloseableHttpClient client;

  private BlobAcquirerRepository blobAcquirerRepository;

  @BeforeEach
  void setup() {
    client = mock(CloseableHttpClient.class);
    blobAcquirerRepository = new BlobAcquirerRepository(
            "http://url:8080",
            "hashPan.zip",
            "apiKey",
            client
    );
  }

  @Test
  void whenSaveExistingFileThenIsUploaded() throws IOException {
    final var toUpload = FileUtils.generateFile("./toUpload.csv", Stream.of("text"));
    final var response = stubResponse(HttpStatus.SC_CREATED);
    final var captor = ArgumentCaptor.forClass(HttpPut.class);
    doReturn(response).when(client).execute(captor.capture());

    assertThat(blobAcquirerRepository.save(new AcquirerFile(toUpload))).isTrue();
    verify(client, times(1)).execute(any(HttpPut.class));
    assertThat(captor.getValue())
            .matches(it -> it.containsHeader("Ocp-Apim-Subscription-Key"))
            .extracting(HttpPut::getEntity)
            .isInstanceOf(FileEntity.class)
            .satisfies(it -> assertThat(it.getContent()).hasContent("text"));

    toUpload.delete();
  }

  @Test
  void whenSaveFailThenReturnFalse() throws IOException {
    final var toUpload = FileUtils.generateFile("./toUpload.csv", Stream.of("text"));
    final var response = stubResponse(HttpStatus.SC_CONFLICT);
    final var captor = ArgumentCaptor.forClass(HttpPut.class);
    doReturn(response).when(client).execute(captor.capture());

    assertThat(blobAcquirerRepository.save(new AcquirerFile(toUpload))).isFalse();
    verify(client, times(1)).execute(any(HttpPut.class));

    toUpload.delete();
  }

  @Test
  void whenUploadThrowsIOExceptionThenSaveReturnFalse() throws IOException {
    final var toUpload = FileUtils.generateFile("./toUpload.csv", Stream.of("text"));
    final var captor = ArgumentCaptor.forClass(HttpPut.class);
    doThrow(new IOException("Error")).when(client).execute(captor.capture());

    assertThat(blobAcquirerRepository.save(new AcquirerFile(toUpload))).isFalse();
    verify(client, times(1)).execute(any(HttpPut.class));

    toUpload.delete();
  }

  private CloseableHttpResponse stubResponse(int status) {
    final var mockedStatusLine = mock(StatusLine.class);
    final var mockedResponse = mock(CloseableHttpResponse.class);
    doReturn(status).when(mockedStatusLine).getStatusCode();
    doReturn("Error").when(mockedStatusLine).getReasonPhrase();
    doReturn(mockedStatusLine).when(mockedResponse).getStatusLine();
    return mockedResponse;
  }
}