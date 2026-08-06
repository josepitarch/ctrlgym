package dev.jpitarch.ctrlgym.payments.utils;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@UtilityClass
public class EpochConverter {

  public static @Nullable LocalDate toLocalDate(Long epoch) {
    if (epoch == null) return null;
    return Instant.ofEpochSecond(epoch).atZone(ZoneId.of("Europe/Madrid")).toLocalDate();
  }

  public static @Nullable ZonedDateTime toZonedDateTime(Long epoch) {
    if (epoch == null) return null;
    return Instant.ofEpochSecond(epoch).atZone(ZoneId.of("Europe/Madrid"));
  }

}
