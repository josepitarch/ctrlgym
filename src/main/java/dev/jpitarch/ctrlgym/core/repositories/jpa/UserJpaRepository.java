package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.UserMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserMO, UserMO.ID> {

  boolean existsByGymIdAndEmail(Integer gymId, String email);

  boolean existsByGymIdNotAndEmail(Integer gymId, String email);

  boolean existsByEmail(String email);

  Optional<UserMO> findByEmail(String email);

  @Query(value = "SELECT EXISTS (SELECT 1 FROM users_migration WHERE email = :email AND gym_id = :gymId)", nativeQuery = true)
  boolean isInMigration(Integer gymId, String email);

  @Query("""
        SELECT m.stripeCustomerId
        FROM UserMO m
        WHERE m.id = :memberId AND m.gymId = :gymId
    """)
  Optional<String> getStripeCustomerId(UUID memberId, Integer gymId);

  @Query("""
        SELECT m.stripeSetupIntentId
        FROM UserMO m
        WHERE m.id = :memberId AND m.gymId = :gymId
    """)
  Optional<String> getStripeSetupIntentId(UUID memberId, Integer gymId);


  @Query("""
        SELECT u.role
        FROM UserMO u
        WHERE u.id = :memberId AND u.gymId = :gymId
    """)
  Optional<String> findRoleById(UUID memberId, Integer gymId);

  @Modifying
  @Transactional
  @Query("UPDATE UserMO u SET u.stripeSetupIntentId = :setupIntentId WHERE u.id = :memberId AND u.gymId = :gymId")
  void saveStripeSetupIntentId(UUID memberId, Integer gymId, String setupIntentId);
}
