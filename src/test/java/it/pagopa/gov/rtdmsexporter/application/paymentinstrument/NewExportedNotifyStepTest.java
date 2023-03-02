package it.pagopa.gov.rtdmsexporter.application.paymentinstrument;

import io.vavr.control.Try;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardPublisher;
import it.pagopa.gov.rtdmsexporter.domain.paymentinstrument.ExportedCardRepository;
import it.pagopa.gov.rtdmsexporter.infrastructure.paymentinstrument.MemoryExportedCardRepository;
import it.pagopa.gov.rtdmsexporter.utils.HashStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
class NewExportedNotifyStepTest {

  @Mock
  private ExportedCardPublisher exportedCardPublisher;
  private NewExportedNotifyStep exportedNotifyStep;
  private ExportedCardRepository exportedCardRepository;

  @BeforeEach
  void setup() {
    exportedCardRepository = new MemoryExportedCardRepository();
    exportedNotifyStep = new NewExportedNotifyStep(exportedCardRepository, exportedCardPublisher);
  }

  @AfterEach
  void clean() {
    Mockito.reset(exportedCardPublisher);
  }

  @Test
  void givenNewExportedCardThenMustConsumeItAndPublishEvent() {
    HashStream.of(20).forEach(exportedCardRepository::save);
    final var toReturn = exportedCardRepository.exportedPaymentInstruments().map(Try::success).toList();
    when(exportedCardPublisher.notifyExportedCard(any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(toReturn));
    assertThat(exportedNotifyStep.execute()).isTrue();
    assertThat(exportedCardRepository.exportedPaymentInstruments()).isEmpty();
    verify(exportedCardPublisher, times(20)).notifyExportedCard(any());
  }

  @Test
  void givenPublishErrorThenContinueToPublishOtherEvent() {
    HashStream.of(20).forEach(exportedCardRepository::save);
    final var toReturn = Stream.concat(
            Stream.of(Try.failure(new Exception("Failed to publish"))),
            exportedCardRepository.exportedPaymentInstruments().skip(1).map(Try::success)
    ).toList();
    when(exportedCardPublisher.notifyExportedCard(any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(toReturn));
    assertThat(exportedNotifyStep.execute()).isTrue();
    assertThat(exportedCardRepository.exportedPaymentInstruments()).hasSize(1);
    verify(exportedCardPublisher, times(20)).notifyExportedCard(any());
  }

}