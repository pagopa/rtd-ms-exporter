package it.pagopa.gov.rtdmsexporter.batch;

import it.pagopa.gov.rtdmsexporter.domain.AcquirerFileRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * A tasklet which download acquire file and unzip it
 */
public class DownloadAcquirerFile implements Tasklet {

  private final AcquirerFileRepository acquirerFileRepository;

  public DownloadAcquirerFile(AcquirerFileRepository acquirerFileRepository) {
    this.acquirerFileRepository = acquirerFileRepository;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    final var acquirerFile = acquirerFileRepository.getAcquirerFile().get();
    if (acquirerFile.file().exists()) {
      contribution.setExitStatus(ExitStatus.COMPLETED);
      chunkContext.setComplete();
      return RepeatStatus.FINISHED;
    }
    contribution.setExitStatus(ExitStatus.FAILED);
    return null;
  }
}
