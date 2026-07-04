package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "memberships")
public class MembershipMO {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "member_id", nullable = false)
  private UUID memberId;

  @Column(name = "gym_id", nullable = false)
  private Integer gymId;

  @Column(name = "membership_plan_id", nullable = false)
  private String membershipPlanId;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "next_billing_date")
  private LocalDate nextBillingDate;

  @ColumnDefault("true")
  @Column(name = "auto_renew", nullable = false)
  private Boolean autoRenew;

  @Column(name = "cancellation_reason_id")
  private Integer cancellationReasonId;

  @Column(name = "stripe_subscription_id")
  private String stripeSubscriptionId;


}
