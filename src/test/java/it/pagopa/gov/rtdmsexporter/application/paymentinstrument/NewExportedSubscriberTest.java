package it.pagopa.gov.rtdmsexporter.application.paymentinstrument;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.mongo.CardEntity;
import it.pagopa.gov.rtdmsexporter.utils.HashStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.time.Duration;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = NewExportedSubscriberTest.Config.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
class NewExportedSubscriberTest {

  private NewExportedSubscriber newExportedSubscriber;

  @Autowired
  private ExportedCardRepository exportedCardRepository;

  @BeforeEach
  void setup() {
    newExportedSubscriber = new NewExportedSubscriber(
            Schedulers.single(),
            exportedCardRepository
    );
  }

  @AfterEach
  void cleanUp() {
    reset(exportedCardRepository);
  }

  @Test
  void whenCardAreNotExportedThenSaveToRepository() {
    final var alreadyExported = HashStream.of(20)
            .map(hash -> new CardEntity(hash, Collections.emptyList(), "", true))
            .toList();

    final var notExported = HashStream.of(20)
            .map(hash -> new CardEntity(hash, Collections.emptyList(), "", false))
            .toList();

    doNothing().when(exportedCardRepository).save(any());
    doReturn(Stream.empty()).when(exportedCardRepository).exportedPaymentInstruments();
    assertThat(newExportedSubscriber.apply(Flowable.just(alreadyExported, notExported))).succeedsWithin(Duration.ofSeconds(5));
    verify(exportedCardRepository, times(20)).save(any());
  }

  @Test
  void whenSourceThrowErrorThenSubscriberFails() {
    assertThat(newExportedSubscriber.apply(Flowable.error(new Error()))).failsWithin(Duration.ofSeconds(5));
    verify(exportedCardRepository, times(0)).save(any());
  }

  @TestConfiguration
  static class Config {
    @Bean
    ExportedCardRepository exportedCardRepository() {
      return mock(ExportedCardRepository.class);
    }
  }
}