package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  private Recurring recurring;

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

  public enum Recurring {
    MONTHLY;

    public static Recurring from(String str) {
      if (!StringUtils.hasText(str)) return null;
      return Recurring.valueOf(str.toUpperCase());
    }

  }

}
