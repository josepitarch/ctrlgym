package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.YearMonth;

public record MemberMetrics(
  @JsonProperty("year_month") YearMonth yearMonth,
  short attendance,
  @JsonProperty("is_closed") boolean isClosed
) {
}
