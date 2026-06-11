package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.GymBranchHeartbeatMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymHeartbeatJpaRepository extends JpaRepository<GymBranchHeartbeatMO, Integer> {
}
