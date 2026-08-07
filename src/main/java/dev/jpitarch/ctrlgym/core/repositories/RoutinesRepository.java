package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.mappers.RoutineMapper;
import dev.jpitarch.ctrlgym.core.models.*;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExerciseJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoutinesRepository {

  private final RoutineJpaRepository routineJpaRepository;

  private final ExerciseJpaRepository exerciseJpaRepository;

  private final RoutineMapper mapper;

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
    return mapper.map(saved);
  }

  public void deleteById(Integer id) {
    routineJpaRepository.deleteById(id);
  }

  public List<Routine> findByMemberId(Member.Id memberId) {
    List<ExerciseMO> exercises = exerciseJpaRepository.findAll();
    return routineJpaRepository
      .findByMemberIdAndGymId(memberId.memberId(), memberId.gymId())
      .stream()
      .map(r -> mapper.mapWithContext(r, exercises))
      .toList();
  }

}
