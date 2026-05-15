package dev.jpitarch.ctrlgym.repositories.jpa;

import dev.jpitarch.ctrlgym.models.GymBranchHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymHeartbeatJpaRepository extends JpaRepository<GymBranchHeartbeat, Integer> {
}
