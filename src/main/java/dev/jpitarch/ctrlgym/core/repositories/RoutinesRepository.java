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

    List<RoutineDayMO> days = new ArrayList<>();
    if (routine.getDays() != null) {
      for (Routine.Day day : routine.getDays()) {
        RoutineDayMO dayMO = new RoutineDayMO();
        dayMO.setRoutineMO(routineMO);
        dayMO.setDayNumber(day.getDayNumber().shortValue());
        dayMO.setName(day.getName());

        List<RoutineDayExerciseMO> exercises = new ArrayList<>();
        if (day.getExercises() != null) {
          for (Routine.Day.Exercise exercise : day.getExercises()) {
            RoutineDayExerciseMO exerciseMO = new RoutineDayExerciseMO();
            exerciseMO.setRoutineDaysMO(dayMO);
            exerciseMO.setExerciseId(exercise.getId());
            exerciseMO.setPosition(exercise.getPosition().shortValue());

if (exercise.getSets() != null && !exercise.getSets().isEmpty()) {
              Set firstSet = exercise.getSets().get(0);
              exerciseMO.setSets((short) exercise.getSets().size());
              exerciseMO.setReps(firstSet.getRepetition().shortValue());
            }
            exercises.add(exerciseMO);
          }
          dayMO.setExercises(exercises);
        }
        days.add(dayMO);
      }
    }
    routineMO.setDays(days);

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
        if (dayMO.getExercises() != null) {
          for (RoutineDayExerciseMO exerciseMO : dayMO.getExercises()) {
            List<Set> sets = new ArrayList<>();
            for (int i = 0; i < exerciseMO.getSets(); i++) {
              sets.add(Set.builder()
                .number(i + 1)
                .repetition(exerciseMO.getReps().intValue())
                .build());
            }
            exercises.add(Routine.Day.Exercise.builder()
              .id(exerciseMO.getExerciseId())
              .position(exerciseMO.getPosition().intValue())
              .sets(sets)
              .build());
          }
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
