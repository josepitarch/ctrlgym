package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.GymBranchHeartbeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface GymHeartbeatJpaRepository extends JpaRepository<GymBranchHeartbeatEntity, Integer> {

  Optional<GymBranchHeartbeatEntity> findTopByGymBranchIdAndCreatedAtAfterOrderByCreatedAtDesc(Integer gymBranchId, OffsetDateTime after);

  @Query("SELECT COUNT(h) FROM GymBranchHeartbeatEntity h WHERE h.gymBranchId = :gymBranchId AND h.createdAt >= :from")
  long countByGymBranchIdSince(Integer gymBranchId, OffsetDateTime from);

}
