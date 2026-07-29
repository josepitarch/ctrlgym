package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.models.MembershipPlanMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipPlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MembershipPlanRepository {

  private final NamedParameterJdbcTemplate jdbc;

  private final MembershipPlanJpaRepository membershipPlanJpaRepository;

  public void create(MembershipPlan membershipPlan, Integer gymId) {
    var plan = new MembershipPlanMO();
    plan.setId(membershipPlan.getId());
    plan.setGymId(gymId);
    plan.setName(membershipPlan.getName());
    plan.setStripePriceId(membershipPlan.getStripePriceId());
    plan.setPrice(java.math.BigDecimal.valueOf(membershipPlan.getPrice()));
    plan.setBillingPeriod(membershipPlan.getRecurring().name());
    plan.setActive(true);
    plan.setCreatedAt(LocalDate.now());
    plan.setGymBranchId(membershipPlan.getGymBranchId());
    plan.setAllBranches(membershipPlan.isAllBranches());

    membershipPlanJpaRepository.save(plan);
  }

  public Optional<MembershipPlan> getById(String id) {
    return this.membershipPlanJpaRepository.findById(id).map(this::map);
  }

  public List<MembershipPlan> getMembershipPlans(GymBranchId gymBranchId) {
    var plans = gymBranchId.branchId() == null
      ? membershipPlanJpaRepository.findByGymIdAndAllBranchesIsTrue(gymBranchId.gymId())
      : membershipPlanJpaRepository.findByGymIdAndGymBranchId(gymBranchId.gymId(), gymBranchId.branchId());

    return plans.stream().map(this::map).toList();
  }

  public void delete(String planId, Integer gymId) {
    MembershipPlanMO planMO = membershipPlanJpaRepository.findById(planId)
      .orElseThrow(() -> new IllegalArgumentException("Membership plan not found"));
    planMO.setDeletedAt(LocalDate.now());
    membershipPlanJpaRepository.save(planMO);
  }




  private MembershipPlan map(MembershipPlanMO plan) {
    return MembershipPlan.builder()
      .id(plan.getId())
      .name(plan.getName())
      .price(plan.getPrice().doubleValue())
      .recurring(MembershipPlan.Recurring.from(plan.getBillingPeriod()))
      .stripePriceId(plan.getStripePriceId())
      .gymBranchId(plan.getGymBranchId())
      .allBranches(plan.getAllBranches())
      .build();
  }
}
