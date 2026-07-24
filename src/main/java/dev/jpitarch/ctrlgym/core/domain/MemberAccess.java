package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAccess {

  @JsonProperty("branch_id")
  private Integer branchId;

  private Direction direction;

  private OffsetDateTime timestamp;

  public enum Direction {
    IN,
    OUT
  }
}
