package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.models.ExerciseMO;
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
    var exerciseMO = new ExerciseMO();
    exerciseMO.setName(exercise.getName());
    exerciseMO.setDescription(exercise.getDescription());
    exerciseMO.setMuscleGroup(exercise.getMuscleGroup());
    exerciseMO.setImage(exercise.getImage());
    exerciseMO.setGymId(gymId);
    ExerciseMO saved = jpaRepository.save(exerciseMO);
    return toDomain(saved);
  }

  public Optional<Exercise> findById(Integer exerciseId) {
    return jpaRepository.findById(exerciseId)
      .map(this::toDomain);
  }

  public void delete(Integer exerciseId) {
    jpaRepository.deleteById(exerciseId);
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
