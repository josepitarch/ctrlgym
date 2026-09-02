package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.mappers.RoutineMapper;
import dev.jpitarch.ctrlgym.core.entities.*;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExerciseJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RoutinesRepository {

  private final RoutineJpaRepository routineJpaRepository;

  private final ExerciseJpaRepository exerciseJpaRepository;

  private final RoutineMapper mapper;

  public Routine save(Routine routine, UUID memberId, Integer gymId) {
    RoutineEntity RoutineEntity = new RoutineEntity();
    RoutineEntity.setName(routine.getName());
    RoutineEntity.setMemberId(memberId);
    RoutineEntity.setGymId(gymId);
    RoutineEntity.setCreatedAt(Instant.now());

    if (routine.getDays() != null) {
      for (Routine.Day day : routine.getDays()) {
        var dayEntity = new RoutineDayEntity();
        dayEntity.setDayNumber(day.getDayNumber().shortValue());
        dayEntity.setName(day.getName());
        dayEntity.setDescription(day.getDescription());

        if (day.getExercises() != null) {
          for (Routine.Day.Exercise exercise : day.getExercises()) {
            var ExerciseEntity = new RoutineDayExerciseEntity();
            ExerciseEntity.setExerciseId(exercise.getId());
            ExerciseEntity.setPosition(exercise.getPosition().shortValue());
            for (var set : exercise.getSets()) {
              var setEntity = new RoutineDayExerciseSetEntity();
              setEntity.setExercise(ExerciseEntity);
              setEntity.setSet(set.getNumber());
              setEntity.setReps(set.getRepetition());
              ExerciseEntity.addSet(setEntity);
            }
            dayEntity.addExercise(ExerciseEntity);
          }
        }
        RoutineEntity.addDay(dayEntity);
      }
    }

    RoutineEntity saved = routineJpaRepository.save(RoutineEntity);
    List<ExerciseEntity> exercises = exerciseJpaRepository.findAll();
    return mapper.mapWithContext(saved, exercises);
  }

  public void deleteById(Integer id) {
    routineJpaRepository.deleteById(id);
  }

  public List<Routine> findByMemberId(UUID memberId) {
    List<ExerciseEntity> exercises = exerciseJpaRepository.findAll();
    return routineJpaRepository
      .findByMemberId(memberId)
      .stream()
      .map(r -> mapper.mapWithContext(r, exercises))
      .toList();
  }

  public Routine saveForGym(Routine routine, Integer gymId) {
    RoutineEntity RoutineEntity = new RoutineEntity();
    RoutineEntity.setName(routine.getName());
    RoutineEntity.setMemberId(null);
    RoutineEntity.setGymId(gymId);
    RoutineEntity.setCreatedAt(Instant.now());

    if (routine.getDays() != null) {
      for (Routine.Day day : routine.getDays()) {
        var dayEntity = new RoutineDayEntity();
        dayEntity.setDayNumber(day.getDayNumber().shortValue());
        dayEntity.setName(day.getName());
        dayEntity.setDescription(day.getDescription());

        if (day.getExercises() != null) {
          for (Routine.Day.Exercise exercise : day.getExercises()) {
            var ExerciseEntity = new RoutineDayExerciseEntity();
            ExerciseEntity.setExerciseId(exercise.getId());
            ExerciseEntity.setPosition(exercise.getPosition().shortValue());
            for (var set : exercise.getSets()) {
              var setEntity = new RoutineDayExerciseSetEntity();
              setEntity.setExercise(ExerciseEntity);
              setEntity.setSet(set.getNumber());
              setEntity.setReps(set.getRepetition());
              ExerciseEntity.addSet(setEntity);
            }
            dayEntity.addExercise(ExerciseEntity);
          }
        }
        RoutineEntity.addDay(dayEntity);
      }
    }

    RoutineEntity saved = routineJpaRepository.save(RoutineEntity);
    List<ExerciseEntity> exercises = exerciseJpaRepository.findAll();
    return mapper.mapWithContext(saved, exercises);
  }

  public List<Routine> findByGymId(Integer gymId) {
    List<ExerciseEntity> exercises = exerciseJpaRepository.findAll();
    return routineJpaRepository
      .findByGymIdAndMemberIdIsNull(gymId)
      .stream()
      .map(r -> mapper.mapWithContext(r, exercises))
      .toList();
  }

}
