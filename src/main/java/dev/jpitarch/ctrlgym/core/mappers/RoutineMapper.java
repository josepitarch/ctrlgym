package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.models.ExerciseMO;
import dev.jpitarch.ctrlgym.core.models.RoutineDayExerciseMO;
import dev.jpitarch.ctrlgym.core.models.RoutineDayExerciseSetMO;
import dev.jpitarch.ctrlgym.core.models.RoutineDayMO;
import dev.jpitarch.ctrlgym.core.models.RoutineMO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(config = BaseMapper.class)
public interface RoutineMapper {

  @Mapping(target = "name", source = "routine.name")
  @Mapping(target = "description", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "memberId", source = "memberId.memberId")
  @Mapping(target = "gymId", source = "memberId.gymId")
  RoutineMO map(Routine routine, Member.Id memberId);

  @Mapping(target = "routine", ignore = true)
  @Mapping(target = "dayNumber", source = "dayNumber")
  RoutineDayMO map(Routine.Day day);

  @Mapping(target = "day", ignore = true)
  @Mapping(target = "exerciseId", source = "id")
  @Mapping(target = "position", source = "position")
  @Mapping(target = "restSeconds", ignore = true)
  RoutineDayExerciseMO map(Routine.Day.Exercise exercise);

  @Mapping(target = "exercise", ignore = true)
  @Mapping(target = "set", source = "number")
  @Mapping(target = "reps", source = "repetition")
  RoutineDayExerciseSetMO map(Routine.Day.Exercise.Set set);

  @Mapping(target = "days", source = "days")
  Routine map(RoutineMO routineMO);

  @Mapping(target = "days", source = "days", qualifiedByName = "mapDayWithContext")
  Routine mapWithContext(RoutineMO routineMO, @Context List<ExerciseMO> exercises);

  @Mapping(target = "exercises", source = "exercises")
  @Mapping(target = "description", ignore = true)
  Routine.Day map(RoutineDayMO dayMO);

  @Named("mapDayWithContext")
  @Mapping(target = "exercises", source = "exercises", qualifiedByName = "mapExerciseWithContext")
  @Mapping(target = "description", ignore = true)
  Routine.Day mapDayWithContext(RoutineDayMO dayMO, @Context List<ExerciseMO> exercises);

  @Mapping(target = "id", source = "exerciseId")
  @Mapping(target = "position", source = "position")
  @Mapping(target = "sets", source = "sets")
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "muscleGroup", ignore = true)
  Routine.Day.Exercise map(RoutineDayExerciseMO exerciseMO);

  @Named("mapExerciseWithContext")
  @Mapping(target = "id", source = "exerciseId")
  @Mapping(target = "position", source = "position")
  @Mapping(target = "sets", source = "sets")
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "muscleGroup", ignore = true)
  Routine.Day.Exercise mapExerciseWithContext(RoutineDayExerciseMO exerciseMO, @Context List<ExerciseMO> exercises);

  @Mapping(target = "number", source = "set")
  @Mapping(target = "repetition", source = "reps")
  Routine.Day.Exercise.Set map(RoutineDayExerciseSetMO setMO);

  @AfterMapping
  default void linkRoutineToDays(@MappingTarget RoutineMO routineMO) {
    if (routineMO.getDays() != null) {
      routineMO.getDays().forEach(day -> day.setRoutine(routineMO));
    }
  }

  @AfterMapping
  default void linkDayToExercises(@MappingTarget RoutineDayMO dayMO) {
    if (dayMO.getExercises() != null) {
      dayMO.getExercises().forEach(exercise -> exercise.setDay(dayMO));
    }
  }

  @AfterMapping
  default void linkExerciseToSets(@MappingTarget RoutineDayExerciseMO exerciseMO) {
    if (exerciseMO.getSets() != null) {
      exerciseMO.getSets().forEach(set -> set.setExercise(exerciseMO));
    }
  }

  @AfterMapping
  default void fillExerciseDetails(@MappingTarget Routine.Day.Exercise exercise, @Context List<ExerciseMO> exercises) {
    if (exercises != null && exercise.getId() != null) {
      exercises.stream()
        .filter(e -> e.getId().equals(exercise.getId()))
        .findFirst()
        .ifPresent(e -> {
          exercise.setName(e.getName());
          exercise.setMuscleGroup(e.getMuscleGroup());
        });
    }
  }
}
