package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.entities.GymBranchEntity;
import dev.jpitarch.ctrlgym.core.entities.GymEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GymJpaRepository extends JpaRepository<GymEntity, Integer> {

  @Query("SELECT g.verifactiApiKey FROM GymEntity g WHERE g.id = :id")
  String findVerifactiApiKeyById(Integer id);

  @Query("SELECT b.apiKey FROM GymEntity g JOIN g.branches b WHERE b.id = :branchId")
  String findControllerApiKey(Integer branchId);

  @Query("SELECT g.stripeAccountId FROM GymEntity g WHERE g.id = :id")
  String findStripeAccountIdById(Integer id);

  @Query("SELECT g.id FROM GymEntity g WHERE g.stripeAccountId = :stripeAccountId")
  Integer findIdByStripeAccountId(String stripeAccountId);

  @Query("SELECT b FROM GymEntity g JOIN g.branches b WHERE g.id = :gymId AND b.id = :branchId")
  GymBranchEntity findBranchByGymIdAndBranchId(Integer gymId, Integer branchId);
}
