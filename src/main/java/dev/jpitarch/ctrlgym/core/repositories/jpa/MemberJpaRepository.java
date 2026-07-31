package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.UserMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberJpaRepository extends JpaRepository<UserMO, UserMO.ID> {

  boolean existsByGymIdAndEmail(Integer gymId, String email);

  boolean existsByGymIdNotAndEmail(Integer gymId, String email);

  @Query(value = "SELECT EXISTS (SELECT 1 FROM users_migration WHERE email = :email AND gym_id = :gymId)", nativeQuery = true)
  boolean isInMigration(Integer gymId, String email);

  @Query("""
        SELECT m.stripeCustomerId
        FROM UserMO m
        WHERE m.id = :memberId AND m.gymId = :gymId
    """)
  Optional<String> getStripeCustomerId(UUID memberId, Integer gymId);

  @Query("""
        SELECT m.stripePaymentMethodId
        FROM UserMO m
        WHERE m.id = :memberId AND m.gymId = :gymId
    """)
  Optional<String> getStripePaymentMethodId(UUID memberId, Integer gymId);

  @Query("""
        SELECT m.stripePaymentMethodId
        FROM UserMO m
        WHERE m.stripeCustomerId = :stripeCustomerId
    """)
  Optional<String> getStripePaymentMethodId(String stripeCustomerId);
}
