package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlan {

  private String id;

  private String name;

  private Double price;

  private Membership.Recurring recurring;

  @JsonIgnore
  private String stripePriceId;

  private Integer gymBranchId;

  @JsonProperty("all_branches")
  private boolean allBranches;

}
