package dev.jpitarch.ctrlgym.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DatePeriodTest {

  static Stream<Arguments> isCurrentTestCases() {
    LocalDate today = LocalDate.now();
    return Stream.of(
      Arguments.of(today.minusDays(10), today.plusDays(10), true, "today is between from and to"),
      Arguments.of(today, today.plusDays(10), true, "today equals from"),
      Arguments.of(today.minusDays(10), today, true, "today equals to"),
      Arguments.of(today.minusDays(10), null, true, "today is after from and to is null"),
      Arguments.of(today, null, true, "today equals from and to is null"),
      Arguments.of(today.plusDays(1), today.plusDays(10), false, "today is before from"),
      Arguments.of(today.minusDays(20), today.minusDays(10), false, "today is after to"),
      Arguments.of(today.plusDays(5), today.plusDays(10), false, "today is before from with future dates"),
      Arguments.of(today.minusDays(20), today.minusDays(10), false, "today is after to with past dates")
    );
  }

  static Stream<Arguments> isPastTestCases() {
    LocalDate today = LocalDate.now();
    return Stream.of(
      Arguments.of(today.minusDays(10), today.minusDays(5), true, "today is after to"),
      Arguments.of(today.minusDays(20), today.minusDays(10), true, "today is well after to"),
      Arguments.of(today.minusDays(10), today, false, "today equals to"),
      Arguments.of(today.minusDays(10), today.plusDays(10), false, "today is before to"),
      Arguments.of(today.minusDays(10), null, false, "to is null"),
      Arguments.of(today, null, false, "to is null even when from is today"),
      Arguments.of(today.plusDays(5), today.plusDays(10), false, "today is before to in future"),
      Arguments.of(today.minusDays(5), today.minusDays(1), true, "today is after to (yesterday)")
    );
  }

  @ParameterizedTest(name = "{3}")
  @MethodSource("isCurrentTestCases")
  @DisplayName("isCurrent returns correct value based on date period")
  void isCurrent_returnsCorrectValue(LocalDate from, LocalDate to, boolean expected, String scenario) {
    DatePeriod period = DatePeriod.of(from, to);
    assertThat(period.isCurrent()).isEqualTo(expected);
  }

  @ParameterizedTest(name = "{3}")
  @MethodSource("isPastTestCases")
  @DisplayName("isPast returns correct value based on date period")
  void isPast_returnsCorrectValue(LocalDate from, LocalDate to, boolean expected, String scenario) {
    DatePeriod period = DatePeriod.of(from, to);
    assertThat(period.isPast()).isEqualTo(expected);
  }
}
