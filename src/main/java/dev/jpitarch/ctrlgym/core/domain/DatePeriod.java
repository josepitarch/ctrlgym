package dev.jpitarch.ctrlgym.core.domain;

import java.time.LocalDate;

public record DatePeriod(LocalDate from, LocalDate to) {

  public static DatePeriod of(LocalDate from, LocalDate to) {
    return new DatePeriod(from, to);
  }
}
