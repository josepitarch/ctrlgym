package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.WorkoutEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkoutJpaRepository extends JpaRepository<WorkoutEntity, Integer> {

  Page<WorkoutEntity> findByMemberId(UUID memberId, Pageable pageable);

  List<WorkoutEntity> findByMemberId(UUID memberId);

}