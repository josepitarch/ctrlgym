package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.entities.RoutineDayEntity;
import dev.jpitarch.ctrlgym.core.entities.WorkoutEntity;
import dev.jpitarch.ctrlgym.core.entities.WorkoutSetEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.WorkoutJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WorkoutsRepository {

  private final WorkoutJpaRepository workoutJpaRepository;

  private final RoutineJpaRepository routineJpaRepository;

  public void save(Workout workout, UUID memberId) {
    var WorkoutEntity = new WorkoutEntity();
    WorkoutEntity.setStartedAt(workout.getStartedAt());
    WorkoutEntity.setFinishedAt(workout.getFinishedAt());
    WorkoutEntity.setStatus(workout.getStatus());
    WorkoutEntity.setMemberId(memberId);

    if (workout.getRoutineId() != null && workout.getDayNumber() != null) {
      RoutineDayEntity routineDay = routineJpaRepository.findDay(workout.getRoutineId(), workout.getDayNumber().shortValue());
      WorkoutEntity.setRoutine(routineDay);
    }

    if (workout.getExercises() != null) {
      for (Workout.Exercise exercise : workout.getExercises()) {
        if (exercise.getSets() != null) {
          for (Workout.Exercise.Set set : exercise.getSets()) {
            var setEntity = new WorkoutSetEntity();
            setEntity.setExerciseId(exercise.getId());
            setEntity.setSet(set.getSetNumber());
            setEntity.setReps(set.getReps());
            WorkoutEntity.addSet(setEntity);
          }
        }
      }
    }

    workoutJpaRepository.save(WorkoutEntity);
  }

  public Page<Workout> findByMemberId(UUID memberId, Pageable pageable) {
    return workoutJpaRepository.findByMemberId(memberId, pageable).map(this::mapToDomain);
  }

  private Workout mapToDomain(WorkoutEntity WorkoutEntity) {
    List<Workout.Exercise> exercises = new ArrayList<>();

    if (WorkoutEntity.getSets() != null) {
      WorkoutEntity.getSets().stream()
        .collect(java.util.stream.Collectors.groupingBy(WorkoutSetEntity::getExerciseId))
        .forEach((exerciseId, sets) -> {
          List<Workout.Exercise.Set> exerciseSets = sets.stream()
            .map(s -> Workout.Exercise.Set.builder()
              .setNumber(s.getSet())
              .reps(s.getReps())
              .build())
            .toList();

          exercises.add(Workout.Exercise.builder()
            .id(exerciseId)
            .sets(exerciseSets)
            .build());
        });
    }

    return Workout.builder()
      .routineId(WorkoutEntity.getRoutine() != null ? WorkoutEntity.getRoutine().getRoutine().getId() : null)
      .dayNumber(WorkoutEntity.getRoutine() != null ? WorkoutEntity.getRoutine().getDayNumber().intValue() : null)
      .startedAt(WorkoutEntity.getStartedAt())
      .finishedAt(WorkoutEntity.getFinishedAt())
      .status(WorkoutEntity.getStatus())
      .exercises(exercises)
      .build();
  }

}
