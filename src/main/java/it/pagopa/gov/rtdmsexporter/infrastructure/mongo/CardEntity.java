package it.pagopa.gov.rtdmsexporter.infrastructure.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document("enrolled_payment_instrument")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CardEntity implements KeyPageableEntity {

  @Indexed(unique = true)
  @Field(name = "hashPan")
  private String hashPan;

  @Indexed
  @Field("hashPanChildren")
  private List<String> hashPanChildren;

  @Field(name = "par")
  private String par;

  @Field(name = "exported")
  private boolean exported = false;

  @Override
  public String getKey() {
    return hashPan;
  }
}
