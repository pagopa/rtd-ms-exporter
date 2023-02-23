package it.pagopa.gov.rtdmsexporter.batch;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.LinkedList;

public class TokenPartitioner implements Tasklet {

  private static final String KEY_NAME = "hashPan";
  private static final String COLLECTION_NAME = "enrolled_payment_instrument";

  private final int partitionSize;
  private final MongoTemplate mongoTemplate;

  public TokenPartitioner(int partitionSize, MongoTemplate mongoTemplate) {
    this.partitionSize = partitionSize;
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    final var queue = new LinkedList<String>();

    final var baseQuery = new Query()
            .with(Sort.by(Sort.Order.asc(KEY_NAME)))
            .limit(1)
            .skip(partitionSize - 1L);
    baseQuery.fields().include(KEY_NAME);

    String token = null;
    boolean end = false;
    while (!end) {
      final var query = Query.of(baseQuery)
              .with(Sort.by(Sort.Order.asc(KEY_NAME)));
      if (token != null) {
        query.addCriteria(Criteria.where(KEY_NAME).gt(token));
      }

      final var items = mongoTemplate.find(query, HashPan.class, COLLECTION_NAME);
      if (!items.isEmpty()) {
        final var key = items.get(0).hashPan;
        queue.push(key);
        token = key;
      }
      end = items.isEmpty();
    }
    // add first page
    queue.push("first_page");
    contribution.getStepExecution().getJobExecution().getExecutionContext().put("partitions", queue);
    contribution.setExitStatus(ExitStatus.COMPLETED);
    return RepeatStatus.FINISHED;
  }

  public record HashPan(
          String hashPan
  ){}
}
