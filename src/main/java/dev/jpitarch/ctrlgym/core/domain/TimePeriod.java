package dev.jpitarch.ctrlgym.core.domain;

import java.time.LocalTime;

public record TimePeriod(LocalTime from, LocalTime to) {

  public static TimePeriod of(LocalTime from, LocalTime to) {
    return new TimePeriod(from, to);
  }

}
