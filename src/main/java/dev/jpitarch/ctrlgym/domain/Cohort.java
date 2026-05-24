package dev.jpitarch.ctrlgym.domain;


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

  private YearMonth yearMonth;

  private Integer offset;

  private Integer activeMembers;

  private Integer cohortSize;

  private Double rate;
}
