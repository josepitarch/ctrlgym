package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.models.*;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExerciseJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoutinesRepository {

  private final RoutineJpaRepository routineJpaRepository;

  private final ExerciseJpaRepository exerciseJpaRepository;

  public Routine save(Routine routine, Member.Id memberId) {
    RoutineMO routineMO = new RoutineMO();
    routineMO.setName(routine.getName());
    routineMO.setMemberId(memberId.memberId());
    routineMO.setGymId(memberId.gymId());
    routineMO.setCreatedAt(Instant.now());

    if (routine.getDays() != null) {
      for (Routine.Day day : routine.getDays()) {
        var dayMO = new RoutineDayMO();
        dayMO.setDayNumber(day.getDayNumber().shortValue());
        dayMO.setName(day.getName());

        if (day.getExercises() != null) {
          for (Routine.Day.Exercise exercise : day.getExercises()) {
            var exerciseMO = new RoutineDayExerciseMO();
            exerciseMO.setExerciseId(exercise.getId());
            exerciseMO.setPosition(exercise.getPosition().shortValue());
            for (var set : exercise.getSets()) {
              var setMO = new RoutineDayExerciseSetMO();
              setMO.setExercise(exerciseMO);
              setMO.setSet(set.getNumber());
              setMO.setReps(set.getRepetition());
              exerciseMO.addSet(setMO);
            }
            dayMO.addExercise(exerciseMO);
          }
        }
        routineMO.addDay(dayMO);
      }
    }

    RoutineMO saved = routineJpaRepository.save(routineMO);
    return mapToDomain(saved, Collections.emptyList());
  }

  public void deleteById(Integer id) {
    routineJpaRepository.deleteById(id);
  }

  public List<Routine> findByMemberId(Member.Id memberId) {
    List<ExerciseMO> exercises = exerciseJpaRepository.findAll();
    return routineJpaRepository
      .findByMemberIdAndGymId(memberId.memberId(), memberId.gymId())
      .stream()
      .map(r -> mapToDomain(r, exercises))
      .toList();
  }

  private Routine mapToDomain(RoutineMO routineMO, List<ExerciseMO> exercisesMO) {
    List<Routine.Day> days = new ArrayList<>();
    if (routineMO.getDays() != null) {
      for (RoutineDayMO dayMO : routineMO.getDays()) {
        List<Routine.Day.Exercise> exercises = new ArrayList<>();

        for (RoutineDayExerciseMO exerciseMO : dayMO.getExercises()) {

          List<Routine.Day.Exercise.Set> sets = new ArrayList<>();
          for (RoutineDayExerciseSetMO setMO : exerciseMO.getSets()) {
            sets.add(Routine.Day.Exercise.Set.builder()
              .number(setMO.getSet())
              .repetition(setMO.getReps())
              .build());
          }

          exercises.add(Routine.Day.Exercise.builder()
            .id(exerciseMO.getExerciseId())
            .name(exercisesMO.stream().filter(e -> e.getId().equals(exerciseMO.getExerciseId())).findFirst().map(ExerciseMO::getName).orElse(null))
            .muscleGroup(exercisesMO.stream().filter(e -> e.getId().equals(exerciseMO.getExerciseId())).findFirst().map(ExerciseMO::getMuscleGroup).orElse(null))
            .position(exerciseMO.getPosition().intValue())
            .sets(sets)
            .build());
        }

        days.add(Routine.Day.builder()
          .dayNumber(dayMO.getDayNumber().intValue())
          .name(dayMO.getName())
          .exercises(exercises)
          .build());
      }
    }
    return Routine.builder()
      .id(routineMO.getId())
      .name(routineMO.getName())
      .days(days)
      .build();
  }

}
