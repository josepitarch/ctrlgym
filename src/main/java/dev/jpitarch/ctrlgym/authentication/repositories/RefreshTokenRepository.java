package dev.jpitarch.ctrlgym.authentication.repositories;

import dev.jpitarch.ctrlgym.authentication.entities.RefreshTokenEntity;
import dev.jpitarch.ctrlgym.authentication.repositories.jpa.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

  private final RefreshTokenJpaRepository jpaRepository;

  public Optional<RefreshTokenEntity> findByTokenHash(String tokenHash) {
    return jpaRepository.findByTokenHash(tokenHash);
  }

  public RefreshTokenEntity save(RefreshTokenEntity refreshToken) {
    return jpaRepository.save(refreshToken);
  }

  public void revokeAllByUserIdAndGymId(UUID userId, Integer gymId) {
    jpaRepository.revokeAllByUserIdAndGymId(userId, gymId);
  }

  public void revokeAllByUserId(UUID userId) {
    jpaRepository.revokeAllByUserId(userId);
  }

  public void setReplacedBy(UUID id, UUID replacedBy) {
    jpaRepository.setReplacedBy(id, replacedBy);
  }

  public int deleteExpiredOrRevoked() {
    return jpaRepository.deleteExpiredOrRevoked(Instant.now());
  }
}
