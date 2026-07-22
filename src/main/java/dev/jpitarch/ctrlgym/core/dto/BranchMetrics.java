package dev.jpitarch.ctrlgym.core.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record BranchMetrics(
  Integer branchId,
  String branchName,
  YearMonth yearMonth,
  BigDecimal revenue,
  short activeMembers,
  short newMembers,
  short churnedMembers,
  BigDecimal churnRate,
  BigDecimal peakOccupancyPct,
  BigDecimal overdueAmount,
  boolean isClosed
) {
}
