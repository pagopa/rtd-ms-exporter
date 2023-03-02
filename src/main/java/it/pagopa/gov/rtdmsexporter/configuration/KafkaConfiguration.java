package it.pagopa.gov.rtdmsexporter.configuration;

import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardPublisher;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.KafkaExportedCardPublisher;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfiguration {

  @Bean
  ExportedCardPublisher exportedCardPublisher(StreamBridge streamBridge) {
    return new KafkaExportedCardPublisher("exporterToPim-out-0", streamBridge);
  }
}
