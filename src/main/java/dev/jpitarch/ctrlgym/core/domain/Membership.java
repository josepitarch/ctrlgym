package dev.jpitarch.ctrlgym.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Membership {

  private Integer id;

  private Integer categoryId;

  private Recurring interval;

  private Double expectedAmount;

  public enum Recurring {
    MONTHLY;

    public static Recurring from(String str) {
      if (!StringUtils.hasText(str)) return null;
      return Recurring.valueOf(str.toUpperCase());
    }
  }

}
