package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Membership {

  private Long id;

  @JsonProperty("membership_plan_id")
  private String planId;

  private MembershipPlan.BillingPeriod billingPeriod;

  @JsonProperty("date_period")
  private DatePeriod datePeriod;

  @JsonProperty("next_billing_date")
  private LocalDate nextBillingDate;

}
