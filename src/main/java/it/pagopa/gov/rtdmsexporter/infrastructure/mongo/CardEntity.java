package it.pagopa.gov.rtdmsexporter.infrastructure.mongo;

import lombok.Getter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document("enrolled_payment_instrument")
@Getter
public class CardEntity {

  @Indexed(unique = true)
  @Field(name = "hashPan")
  private String hashPan;

  @Indexed
  @Field("hashPanChildren")
  private List<String> hashPanChildren;

  @Field(name = "par")
  private String par;

  @Field(name = "exportConfirmed")
  private boolean exportConfirmed;
}
