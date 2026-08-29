package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseJpaRepository extends JpaRepository<ExerciseEntity, Integer> {

  List<ExerciseEntity> findByGymId(Integer gymId);

}
