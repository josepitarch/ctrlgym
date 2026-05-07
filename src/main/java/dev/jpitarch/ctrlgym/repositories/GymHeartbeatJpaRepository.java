package dev.jpitarch.ctrlgym.repositories;

import dev.jpitarch.ctrlgym.models.GymHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymHeartbeatJpaRepository extends JpaRepository<GymHeartbeat, Integer> {
}
