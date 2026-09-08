package dev.jpitarch.ctrlgym.lib;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.Period;

@UtilityClass
public class AgeHelper {

  public static boolean isAdult(LocalDate birthDate) {
    if (birthDate == null) return false;
    return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
  }

}
