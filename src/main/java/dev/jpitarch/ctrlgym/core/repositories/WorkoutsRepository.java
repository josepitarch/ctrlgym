package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.models.RoutineDayMO;
import dev.jpitarch.ctrlgym.core.models.WorkoutMO;
import dev.jpitarch.ctrlgym.core.models.WorkoutSetMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.WorkoutJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WorkoutsRepository {

  private final WorkoutJpaRepository workoutJpaRepository;

  private final RoutineJpaRepository routineJpaRepository;

  public void save(Workout workout, UUID memberId) {
    var workoutMO = new WorkoutMO();
    workoutMO.setStartedAt(workout.getStartedAt());
    workoutMO.setFinishedAt(workout.getFinishedAt());
    workoutMO.setStatus(workout.getStatus());
    workoutMO.setMemberId(memberId);

    if (workout.getRoutineId() != null && workout.getDayNumber() != null) {
      RoutineDayMO routineDay = routineJpaRepository.findDay(workout.getRoutineId(), workout.getDayNumber().shortValue());
      workoutMO.setRoutine(routineDay);
    }

    if (workout.getExercises() != null) {
      for (Workout.Exercise exercise : workout.getExercises()) {
        if (exercise.getSets() != null) {
          for (Workout.Exercise.Set set : exercise.getSets()) {
            var setMO = new WorkoutSetMO();
            setMO.setExerciseId(exercise.getId());
            setMO.setSet(set.getSetNumber());
            setMO.setReps(set.getReps());
            workoutMO.addSet(setMO);
          }
        }
      }
    }

    workoutJpaRepository.save(workoutMO);
  }

  public Page<Workout> findByMemberId(UUID memberId, Pageable pageable) {
    return workoutJpaRepository.findByMemberId(memberId, pageable).map(this::mapToDomain);
  }

  private Workout mapToDomain(WorkoutMO workoutMO) {
    List<Workout.Exercise> exercises = new ArrayList<>();

    if (workoutMO.getSets() != null) {
      workoutMO.getSets().stream()
        .collect(java.util.stream.Collectors.groupingBy(WorkoutSetMO::getExerciseId))
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
      .routineId(workoutMO.getRoutine() != null ? workoutMO.getRoutine().getRoutine().getId() : null)
      .dayNumber(workoutMO.getRoutine() != null ? workoutMO.getRoutine().getDayNumber().intValue() : null)
      .startedAt(workoutMO.getStartedAt())
      .finishedAt(workoutMO.getFinishedAt())
      .status(workoutMO.getStatus())
      .exercises(exercises)
      .build();
  }

}
