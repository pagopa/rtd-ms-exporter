package it.pagopa.gov.rtdmsexporter.configuration;

import io.reactivex.rxjava3.core.Scheduler;
import it.pagopa.gov.rtdmsexporter.application.paymentinstrument.NewExportedNotifyStep;
import it.pagopa.gov.rtdmsexporter.application.paymentinstrument.NewExportedSubscriber;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardPublisher;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.KafkaExportedCardPublisher;
import it.pagopa.gov.rtdmsexporter.infrastructure.paymentinstrument.MemoryExportedCardRepository;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NewExportedModule {

  @Bean
  NewExportedNotifyStep newExportedNotifyStep(
          ExportedCardRepository exportedCardRepository,
          ExportedCardPublisher exportedCardPublisher
  ) {
    return new NewExportedNotifyStep(
            exportedCardRepository,
            exportedCardPublisher
    );
  }

  @Bean
  NewExportedSubscriber newExportedSubscriber(
          Scheduler rxScheduler,
          ExportedCardRepository exportedCardRepository
  ) {
    return new NewExportedSubscriber(rxScheduler, exportedCardRepository);
  }


  @Bean
  ExportedCardPublisher exportedCardPublisher(StreamBridge streamBridge) {
    return new KafkaExportedCardPublisher("exporterToPim-out-0", streamBridge);
  }

  @Bean
  public ExportedCardRepository exportedCardRepository() {
    return new MemoryExportedCardRepository();
  }
}
