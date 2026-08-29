package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.MembershipPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipPlanJpaRepository extends JpaRepository<MembershipPlanEntity, String> {

  List<MembershipPlanEntity> findByGymIdAndAllBranchesIsTrue(Integer gymId);

  List<MembershipPlanEntity> findByGymIdAndGymBranchId(Integer gymId, Integer gymBranchId);

}
