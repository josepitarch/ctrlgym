package dev.jpitarch.ctrlgym.repositories;

import dev.jpitarch.ctrlgym.models.AccessEvent;
import dev.jpitarch.ctrlgym.models.DeviceHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessEventJpaRepository extends JpaRepository<AccessEvent, Integer> {
}
