package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Membership {

  private Integer id;

  private Recurring interval;

  @JsonProperty("date_period")
  private DatePeriod datePeriod;

  @JsonProperty("next_billing_date")
  private LocalDate nextBillingDate;

  public enum Recurring {
    MONTHLY;

    public static Recurring from(String str) {
      if (!StringUtils.hasText(str)) return null;
      return Recurring.valueOf(str.toUpperCase());
    }
  }

}
