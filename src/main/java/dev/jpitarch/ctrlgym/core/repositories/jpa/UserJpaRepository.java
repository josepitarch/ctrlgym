package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

  boolean existsByGymIdAndEmail(Integer gymId, String email);

  boolean existsByGymIdNotAndEmail(Integer gymId, String email);

  boolean existsByEmail(String email);

  Optional<UserEntity> findByEmail(String email);

  List<UserEntity> findAllByEmail(String email);

  Optional<UserEntity> findByEmailAndGymId(String email, Integer gymId);

  @Query(value = "SELECT EXISTS (SELECT 1 FROM users_migration WHERE email = :email AND gym_id = :gymId)", nativeQuery = true)
  boolean isInMigration(Integer gymId, String email);

  @Query("""
        SELECT m.stripeCustomerId
        FROM UserEntity m
        WHERE m.id = :memberId
    """)
  Optional<String> getStripeCustomerId(UUID memberId);

  @Query("""
        SELECT m.stripeSetupIntentId
        FROM UserEntity m
        WHERE m.id = :memberId
    """)
  Optional<String> getStripeSetupIntentId(UUID memberId);


  @Query("""
        SELECT u.role
        FROM UserEntity u
        WHERE u.id = :memberId
    """)
  Optional<String> findRoleById(UUID memberId);

  @Modifying
  @Transactional
  @Query("UPDATE UserEntity u SET u.stripeSetupIntentId = :setupIntentId WHERE u.id = :memberId")
  void saveStripeSetupIntentId(UUID memberId, String setupIntentId);
}
