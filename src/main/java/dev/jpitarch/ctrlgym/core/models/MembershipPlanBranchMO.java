package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Entity
@IdClass(MembershipPlanBranchMO.ID.class)
@Table(name = "membership_plan_branches")
public class MembershipPlanBranchMO {

  @Id
  @Column(name = "membership_plan_id", nullable = false)
  private String membershipPlanId;

  @Id
  @Column(name = "branch_id", nullable = false)
  private Integer branchId;

  @Getter
  @Setter
  @Embeddable
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = 3379217680790866120L;

    private String membershipPlanId;

    private Integer branchId;

  }

}
