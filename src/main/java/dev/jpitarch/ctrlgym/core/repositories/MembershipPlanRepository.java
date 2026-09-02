package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.entities.MembershipPlanEntity;
import dev.jpitarch.ctrlgym.core.mappers.MembershipPlanMapper;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipPlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MembershipPlanRepository {

  private final MembershipPlanJpaRepository membershipPlanJpaRepository;

  private final MembershipPlanMapper mapper;

  public void create(MembershipPlan membershipPlan, Integer gymId, String stripePriceId) {
    var plan = mapper.map(membershipPlan);
    plan.setGymId(gymId);
    plan.setStripePriceId(stripePriceId);
    plan.setActive(true);
    plan.setCreatedAt(LocalDate.now());

    membershipPlanJpaRepository.save(plan);
  }

  public List<MembershipPlan> getMembershipPlans(GymBranchId gymBranchId) {
    var plans = gymBranchId.branchId() == null
      ? membershipPlanJpaRepository.findByGymIdAndAllBranchesIsTrue(gymBranchId.gymId())
      : membershipPlanJpaRepository.findByGymIdAndGymBranchId(gymBranchId.gymId(), gymBranchId.branchId());

    return plans.stream().map(mapper::map).toList();
  }

  public void delete(String planId, Integer gymId) {
    MembershipPlanEntity planEntity = membershipPlanJpaRepository.findById(planId)
      .orElseThrow(() -> new IllegalArgumentException("Membership plan not found"));
    planEntity.setDeletedAt(LocalDate.now());
    membershipPlanJpaRepository.save(planEntity);
  }
}
