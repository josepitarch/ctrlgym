package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.ExerciseMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseJpaRepository extends JpaRepository<ExerciseMO, Integer> {

  List<ExerciseMO> findByGymId(Integer gymId);

}
