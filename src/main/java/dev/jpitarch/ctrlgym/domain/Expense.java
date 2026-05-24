package dev.jpitarch.ctrlgym.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

  private Integer id;

  private Integer categoryId;

  private Nature nature;

  private Frequency frequency;

  private Recurrence recurrence;

  private Double expectedAmount;

  public enum Nature {
    FIXED,
    VARIABLE;

    public static Nature from(String str) {
      return Nature.valueOf(str.toUpperCase());
    }
  }

  public enum Frequency {
    RECURRING,
    ONE_TIME;

    public static Frequency from(String str) {
      return Frequency.valueOf(str.toUpperCase());
    }
  }

  public enum Recurrence {
    WEEKLY,
    MONTHLY;

    public static Recurrence from(String str) {
      if (!StringUtils.hasText(str)) return null;
      return Recurrence.valueOf(str.toUpperCase());
    }
  }

}
