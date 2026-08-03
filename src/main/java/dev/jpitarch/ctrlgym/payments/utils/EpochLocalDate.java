package dev.jpitarch.ctrlgym.payments.utils;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@UtilityClass
public class EpochLocalDate {

  public static LocalDate toLocalDate(long epoch) {
    return Instant.ofEpochSecond(epoch).atZone(ZoneId.of("Europe/Madrid")).toLocalDate();
  }

}
