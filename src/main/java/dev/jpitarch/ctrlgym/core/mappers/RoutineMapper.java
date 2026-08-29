package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.entities.ExerciseEntity;
import dev.jpitarch.ctrlgym.core.entities.RoutineDayExerciseEntity;
import dev.jpitarch.ctrlgym.core.entities.RoutineDayExerciseSetEntity;
import dev.jpitarch.ctrlgym.core.entities.RoutineDayEntity;
import dev.jpitarch.ctrlgym.core.entities.RoutineEntity;
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
  RoutineEntity map(Routine routine, Member.Id memberId);

  @Mapping(target = "routine", ignore = true)
  @Mapping(target = "dayNumber", source = "dayNumber")
  RoutineDayEntity map(Routine.Day day);

  @Mapping(target = "day", ignore = true)
  @Mapping(target = "exerciseId", source = "id")
  @Mapping(target = "position", source = "position")
  @Mapping(target = "restSeconds", ignore = true)
  RoutineDayExerciseEntity map(Routine.Day.Exercise exercise);

  @Mapping(target = "exercise", ignore = true)
  @Mapping(target = "set", source = "number")
  @Mapping(target = "reps", source = "repetition")
  RoutineDayExerciseSetEntity map(Routine.Day.Exercise.Set set);

  @Mapping(target = "days", source = "days")
  Routine map(RoutineEntity RoutineEntity);

  @Mapping(target = "days", source = "days", qualifiedByName = "mapDayWithContext")
  Routine mapWithContext(RoutineEntity RoutineEntity, @Context List<ExerciseEntity> exercises);

  @Mapping(target = "exercises", source = "exercises")
  @Mapping(target = "description", ignore = true)
  Routine.Day map(RoutineDayEntity dayEntity);

  @Named("mapDayWithContext")
  @Mapping(target = "exercises", source = "exercises", qualifiedByName = "mapExerciseWithContext")
  @Mapping(target = "description", ignore = true)
  Routine.Day mapDayWithContext(RoutineDayEntity dayEntity, @Context List<ExerciseEntity> exercises);

  @Mapping(target = "id", source = "exerciseId")
  @Mapping(target = "position", source = "position")
  @Mapping(target = "sets", source = "sets")
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "muscleGroup", ignore = true)
  Routine.Day.Exercise map(RoutineDayExerciseEntity ExerciseEntity);

  @Named("mapExerciseWithContext")
  @Mapping(target = "id", source = "exerciseId")
  @Mapping(target = "position", source = "position")
  @Mapping(target = "sets", source = "sets")
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "muscleGroup", ignore = true)
  Routine.Day.Exercise mapExerciseWithContext(RoutineDayExerciseEntity ExerciseEntity, @Context List<ExerciseEntity> exercises);

  @Mapping(target = "number", source = "set")
  @Mapping(target = "repetition", source = "reps")
  Routine.Day.Exercise.Set map(RoutineDayExerciseSetEntity setEntity);

  @AfterMapping
  default void linkRoutineToDays(@MappingTarget RoutineEntity RoutineEntity) {
    if (RoutineEntity.getDays() != null) {
      RoutineEntity.getDays().forEach(day -> day.setRoutine(RoutineEntity));
    }
  }

  @AfterMapping
  default void linkDayToExercises(@MappingTarget RoutineDayEntity dayEntity) {
    if (dayEntity.getExercises() != null) {
      dayEntity.getExercises().forEach(exercise -> exercise.setDay(dayEntity));
    }
  }

  @AfterMapping
  default void linkExerciseToSets(@MappingTarget RoutineDayExerciseEntity ExerciseEntity) {
    if (ExerciseEntity.getSets() != null) {
      ExerciseEntity.getSets().forEach(set -> set.setExercise(ExerciseEntity));
    }
  }

  @AfterMapping
  default void fillExerciseDetails(@MappingTarget Routine.Day.Exercise exercise, @Context List<ExerciseEntity> exercises) {
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
