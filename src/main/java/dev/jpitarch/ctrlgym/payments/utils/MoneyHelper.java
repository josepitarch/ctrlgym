package dev.jpitarch.ctrlgym.payments.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class MoneyHelper {

  public static BigDecimal toEuros(long cents) {
    return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
  }

}
