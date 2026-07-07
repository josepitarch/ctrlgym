package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.MembershipPlanMO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipPlanJpaRepository extends JpaRepository<MembershipPlanMO, String> {

  List<MembershipPlanMO> findByGymId(Integer gymId);

  void deleteByIdAndGymId(String id, Integer gymId);

}
