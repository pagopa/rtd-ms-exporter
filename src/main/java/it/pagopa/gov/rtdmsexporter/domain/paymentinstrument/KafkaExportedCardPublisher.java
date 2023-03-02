package it.pagopa.gov.rtdmsexporter.domain.paymentinstrument;

import io.vavr.control.Try;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;

public class KafkaExportedCardPublisher implements ExportedCardPublisher {

  private final String outputBindingName;
  private final StreamBridge streamBridge;

  public KafkaExportedCardPublisher(String outputBindingName, StreamBridge streamBridge) {
    this.outputBindingName = outputBindingName;
    this.streamBridge = streamBridge;
  }

  @Override
  public Try<String> notifyExportedCard(String cardId) {
    final var event = new SimpleCloudEventDto<>("confirmExport", new ExportCardEventDto(cardId));
    final var message = MessageBuilder.withPayload(event).setHeader("partitionKey", cardId).build();
    return Try.of(() -> streamBridge.send(outputBindingName, message))
            .flatMap(isPublish -> isPublish ? Try.success(cardId) : Try.failure(new RuntimeException("Failed to send export event")));
  }

  record SimpleCloudEventDto<T>(
          String type,
          T data
  ) {}

  record ExportCardEventDto(
     String paymentInstrumentId
  ) {}
}
