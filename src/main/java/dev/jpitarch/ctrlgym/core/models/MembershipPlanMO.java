package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "membership_plans")
public class MembershipPlanMO {
  @Id
  @Column(name = "id", nullable = false, length = Integer.MAX_VALUE)
  private String id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "billing_period", nullable = false, length = 20)
  private String billingPeriod;

  @ColumnDefault("true")
  @Column(name = "active", nullable = false)
  private Boolean active;

  @ColumnDefault("CURRENT_DATE")
  @Column(name = "created_at", nullable = false)
  private LocalDate createdAt;

  @Column(name = "stripe_price_id", nullable = false)
  private String stripePriceId;

  @OneToMany
  private List<MembershipPlanBranchMO> branches = new ArrayList<>();

}
