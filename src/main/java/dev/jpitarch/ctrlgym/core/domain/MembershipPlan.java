package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlan {

  private String id;

  private String name;

  private Double price;

  private Recurring recurring;

  //TODO: eliminar esta propiedad del dominio
  @JsonIgnore
  private String stripePriceId;

  private Integer gymBranchId;

  @JsonProperty("all_branches")
  private boolean allBranches;

  public enum Recurring {
    MONTHLY;

    public static Recurring from(String str) {
      if (!StringUtils.hasText(str)) return null;
      return Recurring.valueOf(str.toUpperCase());
    }

  }

}
