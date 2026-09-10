package dev.jpitarch.ctrlgym.core.domain;

import java.time.LocalDate;

public record DateRange(LocalDate from, LocalDate to) {

  public static DateRange of(LocalDate from, LocalDate to) {
    return new DateRange(from, to);
  }

  public boolean isActive() {
    var today = LocalDate.now();
    return !today.isBefore(from) && (to == null || !today.isAfter(to));
  }

  public boolean isPast() {
    var today = LocalDate.now();
    return to != null && today.isAfter(to);
  }

}
