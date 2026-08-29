package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.entities.MembershipPlanEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipPlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MembershipPlanRepository {

  private final NamedParameterJdbcTemplate jdbc;

  private final MembershipPlanJpaRepository membershipPlanJpaRepository;

  public void create(MembershipPlan membershipPlan, Integer gymId, String stripePriceId) {
    var plan = new MembershipPlanEntity();
    plan.setId(membershipPlan.getId());
    plan.setGymId(gymId);
    plan.setName(membershipPlan.getName());
    plan.setStripePriceId(stripePriceId);
    plan.setPrice(java.math.BigDecimal.valueOf(membershipPlan.getPrice()));
    plan.setBillingPeriod(membershipPlan.getRecurring().name());
    plan.setActive(true);
    plan.setCreatedAt(LocalDate.now());
    plan.setGymBranchId(membershipPlan.getGymBranchId());
    plan.setAllBranches(membershipPlan.isAllBranches());

    membershipPlanJpaRepository.save(plan);
  }

  public List<MembershipPlan> getMembershipPlans(GymBranchId gymBranchId) {
    var plans = gymBranchId.branchId() == null
      ? membershipPlanJpaRepository.findByGymIdAndAllBranchesIsTrue(gymBranchId.gymId())
      : membershipPlanJpaRepository.findByGymIdAndGymBranchId(gymBranchId.gymId(), gymBranchId.branchId());

    return plans.stream().map(this::map).toList();
  }

  public void delete(String planId, Integer gymId) {
    MembershipPlanEntity planEntity = membershipPlanJpaRepository.findById(planId)
      .orElseThrow(() -> new IllegalArgumentException("Membership plan not found"));
    planEntity.setDeletedAt(LocalDate.now());
    membershipPlanJpaRepository.save(planEntity);
  }

  private MembershipPlan map(MembershipPlanEntity plan) {
    return MembershipPlan.builder()
      .id(plan.getId())
      .name(plan.getName())
      .price(plan.getPrice().doubleValue())
      .recurring(MembershipPlan.Recurring.from(plan.getBillingPeriod()))
      .gymBranchId(plan.getGymBranchId())
      .allBranches(plan.getAllBranches())
      .build();
  }
}
