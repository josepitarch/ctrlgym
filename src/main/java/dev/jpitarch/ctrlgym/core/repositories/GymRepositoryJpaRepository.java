package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.models.GymMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GymRepositoryJpaRepository extends JpaRepository<GymMO, Integer> {

  @Query("SELECT g.verifactiApiKey FROM GymMO g WHERE g.id = :id")
  String findVerifactiApiKeyById(Integer id);

  @Query("SELECT b.apiKey FROM GymMO g JOIN g.branches b WHERE b.id = :branchId")
  String findControllerApiKey(Integer branchId);

  @Query("SELECT g.stripeAccountId FROM GymMO g WHERE g.id = :id")
  String findStripeAccountIdById(Integer id);

  @Query("SELECT g.id FROM GymMO g WHERE g.stripeAccountId = :stripeAccountId")
  Integer findIdByStripeAccountId(String stripeAccountId);
}
