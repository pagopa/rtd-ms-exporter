package it.pagopa.gov.rtdmsexporter.infrastructure;

public record BlobConfig(
        String baseUrl,
        String apiKey,
        String containerName
) {
}
