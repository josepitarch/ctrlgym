package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.YearMonth;

public record BranchMetrics(
  @JsonProperty("branch_id") Integer branchId,
  @JsonProperty("branch_name") String branchName,
  @JsonProperty("year_month") YearMonth yearMonth,
  BigDecimal revenue,
  @JsonProperty("active_members") short activeMembers,
  @JsonProperty("new_members") short newMembers,
  @JsonProperty("churned_members") short churnedMembers,
  @JsonProperty("churn_rate") BigDecimal churnRate,
  @JsonProperty("peak_occupancy_pct") BigDecimal peakOccupancyPct,
  @JsonProperty("overdue_amount") BigDecimal overdueAmount,
  @JsonProperty("is_closed") boolean isClosed
) {
}
