package dev.jpitarch.ctrlgym.core.domain;

import java.time.LocalDate;

public record DatePeriod(LocalDate from, LocalDate to) {

  public static DatePeriod of(LocalDate from, LocalDate to) {
    return new DatePeriod(from, to);
  }

  public boolean isCurrent() {
    var today = LocalDate.now();
    return !today.isBefore(from) && (to == null || !today.isAfter(to));
  }

  public boolean isPast() {
    var today = LocalDate.now();
    return to != null && today.isAfter(to);
  }
}
