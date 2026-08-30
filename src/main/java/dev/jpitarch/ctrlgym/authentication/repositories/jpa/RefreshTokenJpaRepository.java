package dev.jpitarch.ctrlgym.authentication.repositories.jpa;

import dev.jpitarch.ctrlgym.authentication.entities.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

  Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

  @Modifying
  @Transactional
  @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userId = :userId AND r.gymId = :gymId")
  void revokeAllByUserIdAndGymId(UUID userId, Integer gymId);

  @Modifying
  @Transactional
  @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userId = :userId")
  void revokeAllByUserId(UUID userId);

  @Modifying
  @Transactional
  @Query("UPDATE RefreshTokenEntity r SET r.replacedBy = :replacedBy WHERE r.id = :id")
  void setReplacedBy(UUID id, UUID replacedBy);

  @Modifying
  @Transactional
  @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiresAt < :now OR r.revoked IS TRUE")
  int deleteExpiredOrRevoked(Instant now);
}
