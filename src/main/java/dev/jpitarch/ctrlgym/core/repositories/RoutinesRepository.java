package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.domain.Routine.Day.Exercise.Set;
import dev.jpitarch.ctrlgym.core.models.RoutineDayExerciseMO;
import dev.jpitarch.ctrlgym.core.models.RoutineDayMO;
import dev.jpitarch.ctrlgym.core.models.RoutineMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RoutinesRepository {

  private final RoutineJpaRepository routineJpaRepository;

  public Routine save(Routine routine, UUID memberId, Integer gymId) {
    RoutineMO routineMO = new RoutineMO();
    routineMO.setName(routine.getName());
    routineMO.setMemberId(memberId);
    routineMO.setGymId(gymId);
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
            exerciseMO.setSets((short) 4);
            exerciseMO.setReps((short) 10);
            dayMO.addExercise(exerciseMO);
          }
        }
        routineMO.addDay(dayMO);
      }
    }

    RoutineMO saved = routineJpaRepository.save(routineMO);
    return mapToDomain(saved);
  }

  public void deleteById(Integer id) {
    RoutineMO routine = routineJpaRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Routine not found with id: " + id));
    routine.setDeletedAt(LocalDate.now());
    routineJpaRepository.save(routine);
  }

  private Routine mapToDomain(RoutineMO routineMO) {
    List<Routine.Day> days = new ArrayList<>();
    if (routineMO.getDays() != null) {
      for (RoutineDayMO dayMO : routineMO.getDays()) {
        List<Routine.Day.Exercise> exercises = new ArrayList<>();

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
