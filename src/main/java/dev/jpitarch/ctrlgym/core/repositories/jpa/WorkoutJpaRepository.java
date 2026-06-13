package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.WorkoutMO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkoutJpaRepository extends JpaRepository<WorkoutMO, Integer> {

  Page<WorkoutMO> findByMemberId(UUID memberId, Pageable pageable);

}