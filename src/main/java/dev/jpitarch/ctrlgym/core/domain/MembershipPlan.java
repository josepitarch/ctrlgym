package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlan {

  private String id;

  private String name;

  private Double price;

  private BillingPeriod billingPeriod;

  @JsonProperty("gym_branch_id")
  private Integer gymBranchId;

  @JsonProperty("all_branches")
  private boolean allBranches;

  @JsonProperty("from")
  private LocalTime from;

  @JsonProperty("to")
  private LocalTime to;

  @JsonProperty("all_day")
  private Boolean allDay;

  public enum BillingPeriod {
    MONTHLY,
    QUARTERLY,
    SEMI_ANNUAL,
    ANNUAL;

    public static BillingPeriod from(String str) {
      if (!StringUtils.hasText(str)) return null;
      return BillingPeriod.valueOf(str.toUpperCase());
    }

  }

}
