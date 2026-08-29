package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.entities.ExerciseEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExerciseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

  public Exercise create(Exercise exercise, Integer gymId) {
    var ExerciseEntity = new ExerciseEntity();
    ExerciseEntity.setName(exercise.getName());
    ExerciseEntity.setDescription(exercise.getDescription());
    ExerciseEntity.setMuscleGroup(exercise.getMuscleGroup());
    ExerciseEntity.setImage(exercise.getImage());
    ExerciseEntity.setGymId(gymId);
    ExerciseEntity saved = jpaRepository.save(ExerciseEntity);
    return toDomain(saved);
  }

  public Optional<Exercise> findById(Integer exerciseId) {
    return jpaRepository.findById(exerciseId)
      .map(this::toDomain);
  }

  public void delete(Integer exerciseId) {
    jpaRepository.deleteById(exerciseId);
  }

  private Exercise toDomain(ExerciseEntity ExerciseEntity) {
    return Exercise.builder()
      .id(ExerciseEntity.getId())
      .name(ExerciseEntity.getName())
      .description(ExerciseEntity.getDescription())
      .muscleGroup(ExerciseEntity.getMuscleGroup())
      .image(ExerciseEntity.getImage())
      .build();
  }

}
