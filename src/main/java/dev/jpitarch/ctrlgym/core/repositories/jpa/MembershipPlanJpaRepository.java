package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.MembershipPlanMO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipPlanJpaRepository extends JpaRepository<MembershipPlanMO, String> {

  List<MembershipPlanMO> findByGymIdAndAllBranchesIsTrue(Integer gymId);

  List<MembershipPlanMO> findByGymIdAndGymBranchId(Integer gymId, Integer gymBranchId);

}
