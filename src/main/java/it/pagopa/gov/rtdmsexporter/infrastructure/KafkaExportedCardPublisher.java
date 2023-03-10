package it.pagopa.gov.rtdmsexporter.infrastructure;

import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardPublisher;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;

public class KafkaExportedCardPublisher implements ExportedCardPublisher {

  public static final String EVENT_TYPE = "ConfirmExport";
  private final String outputBindingName;
  private final StreamBridge streamBridge;

  public KafkaExportedCardPublisher(String outputBindingName, StreamBridge streamBridge) {
    this.outputBindingName = outputBindingName;
    this.streamBridge = streamBridge;
  }

  @Override
  public Try<String> notifyExportedCard(String cardId) {
    final var event = new SimpleCloudEventDto<>(EVENT_TYPE, new ExportCardEventDto(cardId));
    final var message = MessageBuilder.withPayload(event).setHeader("partitionKey", cardId).build();
    return Try.of(() -> streamBridge.send(outputBindingName, message))
            .flatMap(isPublish -> Boolean.TRUE.equals(isPublish) ? Try.success(cardId) : Try.failure(new RuntimeException("Failed to send export event")));
  }

  record SimpleCloudEventDto<T>(
          String type,
          T data
  ) {}

  record ExportCardEventDto(
     String paymentInstrumentId
  ) {}
}
