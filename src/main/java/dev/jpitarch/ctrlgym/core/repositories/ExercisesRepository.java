package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.models.ExerciseMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExerciseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExercisesRepository {

  private final ExerciseJpaRepository jpaRepository;

  public List<Exercise> getAll(Integer gymId) {
    return jpaRepository.findByGymId(gymId)
      .stream()
      .map(this::toDomain)
      .toList();
  }

  private Exercise toDomain(ExerciseMO exerciseMO) {
    return Exercise.builder()
      .id(exerciseMO.getId())
      .name(exerciseMO.getName())
      .description(exerciseMO.getDescription())
      .muscleGroup(exerciseMO.getMuscleGroup())
      .image(exerciseMO.getImage())
      .build();
  }

}
