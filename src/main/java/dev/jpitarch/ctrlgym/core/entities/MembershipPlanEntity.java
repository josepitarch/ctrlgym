package dev.jpitarch.ctrlgym.core.entities;

import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "membership_plans")
@SQLRestriction("deleted_at IS NULL")
public class MembershipPlanEntity {
  @Id
  @Column(name = "id", nullable = false, length = Integer.MAX_VALUE)
  private String id;

  @Column(name = "gym_id")
  private Integer gymId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "billing_period", nullable = false)
  private MembershipPlan.BillingPeriod billingPeriod;

  @ColumnDefault("true")
  @Column(name = "active", nullable = false)
  private Boolean active;

  @ColumnDefault("CURRENT_DATE")
  @Column(name = "created_at", nullable = false)
  private LocalDate createdAt;

  @Column(name = "stripe_price_id", nullable = false)
  private String stripePriceId;

  @Column(name = "gym_branch_id")
  private Integer gymBranchId;

  @Column(name = "all_branches")
  private Boolean allBranches;

  @Column(name = "deleted_at")
  private LocalDate deletedAt;

  @Column(name = "start_time")
  private LocalTime startTime;

  @Column(name = "end_time")
  private LocalTime endTime;

  @Column(name = "all_day")
  private Boolean allDay;

}
