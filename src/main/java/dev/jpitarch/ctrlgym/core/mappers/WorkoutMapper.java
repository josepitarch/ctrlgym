package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.entities.WorkoutEntity;
import dev.jpitarch.ctrlgym.core.entities.WorkoutSetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(config = BaseMapper.class, uses = {})
public interface WorkoutMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "routine", ignore = true)
  @Mapping(target = "memberId", source = "memberId")
  @Mapping(target = "startedAt", source = "workout.startedAt")
  @Mapping(target = "finishedAt", source = "workout.finishedAt")
  @Mapping(target = "status", source = "workout.status")
  @Mapping(target = "sets", expression = "java(mapExercisesToSets(workout.getExercises()))")
  WorkoutEntity map(Workout workout, UUID memberId);

  @Mapping(target = "routineId", expression = "java(entity.getRoutine() != null ? entity.getRoutine().getRoutine().getId() : null)")
  @Mapping(target = "dayNumber", expression = "java(entity.getRoutine() != null ? (int) entity.getRoutine().getDayNumber() : null)")
  @Mapping(target = "startedAt", source = "startedAt")
  @Mapping(target = "finishedAt", source = "finishedAt")
  @Mapping(target = "status", source = "status")
  @Mapping(target = "exercises", expression = "java(mapSetsToExercises(entity.getSets()))")
  Workout map(WorkoutEntity entity);

  default WorkoutSetEntity mapSet(Workout.Exercise.Set set, Integer exerciseId) {
    if (set == null) {
      return null;
    }
    WorkoutSetEntity entity = new WorkoutSetEntity();
    entity.setExerciseId(exerciseId);
    entity.setSet(set.getSetNumber());
    entity.setReps(set.getReps());
    return entity;
  }

  default List<WorkoutSetEntity> mapExercisesToSets(List<Workout.Exercise> exercises) {
    if (exercises == null) {
      return new ArrayList<>();
    }
    List<WorkoutSetEntity> result = new ArrayList<>();
    for (Workout.Exercise exercise : exercises) {
      if (exercise.getSets() != null) {
        for (Workout.Exercise.Set set : exercise.getSets()) {
          result.add(mapSet(set, exercise.getId()));
        }
      }
    }
    return result;
  }

  default Workout.Exercise.Set mapSet(WorkoutSetEntity setEntity) {
    if (setEntity == null) {
      return null;
    }
    return Workout.Exercise.Set.builder()
      .setNumber(setEntity.getSet())
      .reps(setEntity.getReps())
      .build();
  }

  default List<Workout.Exercise> mapSetsToExercises(List<WorkoutSetEntity> sets) {
    if (sets == null) {
      return new ArrayList<>();
    }
    Map<Integer, List<WorkoutSetEntity>> groupedByExercise = sets.stream()
      .collect(Collectors.groupingBy(WorkoutSetEntity::getExerciseId));

    List<Workout.Exercise> result = new ArrayList<>();
    for (Map.Entry<Integer, List<WorkoutSetEntity>> entry : groupedByExercise.entrySet()) {
      List<Workout.Exercise.Set> exerciseSets = new ArrayList<>();
      for (WorkoutSetEntity setEntity : entry.getValue()) {
        exerciseSets.add(mapSet(setEntity));
      }
      result.add(Workout.Exercise.builder()
        .id(entry.getKey())
        .sets(exerciseSets)
        .build());
    }
    return result;
  }
}
