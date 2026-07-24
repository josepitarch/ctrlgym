package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.GymBranchHeartbeatMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface GymHeartbeatJpaRepository extends JpaRepository<GymBranchHeartbeatMO, Integer> {

  Optional<GymBranchHeartbeatMO> findTopByGymBranchIdOrderByCreatedAtDesc(Integer gymBranchId);

  @Query("SELECT COUNT(h) FROM GymBranchHeartbeatMO h WHERE h.gymBranchId = :gymBranchId AND h.createdAt >= :from")
  long countByGymBranchIdSince(Integer gymBranchId, OffsetDateTime from);

}
