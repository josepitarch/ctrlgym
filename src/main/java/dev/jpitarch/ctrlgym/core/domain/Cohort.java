package dev.jpitarch.ctrlgym.core.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cohort {

  @JsonProperty("year_month")
  private YearMonth yearMonth;

  private Integer offset;

  @JsonProperty("active_members")
  private Integer activeMembers;

  @JsonProperty("cohort_size")
  private Integer cohortSize;

  private Double rate;
}
