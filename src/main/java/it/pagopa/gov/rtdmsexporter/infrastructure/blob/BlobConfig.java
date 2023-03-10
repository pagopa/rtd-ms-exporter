package it.pagopa.gov.rtdmsexporter.infrastructure.blob;

public record BlobConfig(
        String baseUrl,
        String apiKey,
        String containerName
) {

  public static BlobConfig of(String baseUrl, String apiKey, String containerName) {
    return new BlobConfig(baseUrl, apiKey, containerName);
  }
}
