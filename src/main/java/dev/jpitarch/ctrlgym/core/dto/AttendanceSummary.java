package dev.jpitarch.ctrlgym.core.dto;

import java.time.LocalDate;
import java.util.List;


public record AttendanceSummary(LocalDate from, LocalDate to,
                                AttendanceGroupBy groupBy,
                                Summary summary,
                                List<AttendancePeriod> periods
) {
  public record Summary(int count) {
  }

  public record AttendancePeriod(LocalDate from, LocalDate to, int daysAttended) {
  }

  public enum AttendanceGroupBy {DAY, WEEK, MONTH}
}
