package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.dto.PersonalRecordResult;
import dev.jpitarch.ctrlgym.core.dto.PersonalRecordResult.RecordType;
import dev.jpitarch.ctrlgym.core.entities.MemberPersonalRecordEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberPersonalRecordJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalRecordServiceTest {

  @InjectMocks
  private PersonalRecordService personalRecordService;

  @Mock
  private MemberPersonalRecordJpaRepository jpaRepository;

  private final UUID memberId = UUID.randomUUID();

  @Test
  @DisplayName("Should create first personal record when no previous record exists")
  void shouldCreateFirstPersonalRecord() {
    Workout workout = createWorkout(1, List.of(createSet(10, 80.0)));

    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 1))
      .thenReturn(Collections.emptyList());
    when(jpaRepository.save(any(MemberPersonalRecordEntity.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().exerciseId()).isEqualTo(1);
    assertThat(results.getFirst().weight()).isEqualTo(80.0);
    assertThat(results.getFirst().reps()).isEqualTo(10);
    assertThat(results.getFirst().recordType()).isEqualTo(RecordType.WEIGHT);
    assertThat(results.getFirst().deltaWeight()).isEqualTo(0);
    assertThat(results.getFirst().deltaReps()).isEqualTo(0);
    verify(jpaRepository).save(any(MemberPersonalRecordEntity.class));
  }

  @Test
  @DisplayName("Should create personal record when weight increases")
  void shouldCreatePersonalRecordWhenWeightIncreases() {
    Workout workout = createWorkout(1, List.of(createSet(10, 85.0)));

    MemberPersonalRecordEntity previousRecord = createPreviousRecord(80.0, 10);

    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 1))
      .thenReturn(List.of(previousRecord));
    when(jpaRepository.save(any(MemberPersonalRecordEntity.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().weight()).isEqualTo(85.0);
    assertThat(results.getFirst().reps()).isEqualTo(10);
    assertThat(results.getFirst().recordType()).isEqualTo(RecordType.WEIGHT);
    assertThat(results.getFirst().deltaWeight()).isEqualTo(5.0);
    assertThat(results.getFirst().deltaReps()).isEqualTo(0);
    verify(jpaRepository).save(any(MemberPersonalRecordEntity.class));
  }

  @Test
  @DisplayName("Should create personal record when reps increase with same weight")
  void shouldCreatePersonalRecordWhenRepsIncreaseWithSameWeight() {
    Workout workout = createWorkout(1, List.of(createSet(12, 80.0)));

    MemberPersonalRecordEntity previousRecord = createPreviousRecord(80.0, 10);

    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 1))
      .thenReturn(List.of(previousRecord));
    when(jpaRepository.save(any(MemberPersonalRecordEntity.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().weight()).isEqualTo(80.0);
    assertThat(results.getFirst().reps()).isEqualTo(12);
    assertThat(results.getFirst().recordType()).isEqualTo(RecordType.REPS);
    assertThat(results.getFirst().deltaWeight()).isEqualTo(0);
    assertThat(results.getFirst().deltaReps()).isEqualTo(2);
    verify(jpaRepository).save(any(MemberPersonalRecordEntity.class));
  }

  @Test
  @DisplayName("Should not create personal record when weight decreases")
  void shouldNotCreatePersonalRecordWhenWeightDecreases() {
    Workout workout = createWorkout(1, List.of(createSet(10, 75.0)));

    MemberPersonalRecordEntity previousRecord = createPreviousRecord(80.0, 10);

    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 1))
      .thenReturn(List.of(previousRecord));

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).isEmpty();
    verify(jpaRepository, never()).save(any(MemberPersonalRecordEntity.class));
  }

  @Test
  @DisplayName("Should not create personal record when same weight and same reps")
  void shouldNotCreatePersonalRecordWhenSameWeightAndSameReps() {
    Workout workout = createWorkout(1, List.of(createSet(10, 80.0)));

    MemberPersonalRecordEntity previousRecord = createPreviousRecord(80.0, 10);

    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 1))
      .thenReturn(List.of(previousRecord));

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).isEmpty();
    verify(jpaRepository, never()).save(any(MemberPersonalRecordEntity.class));
  }

  @Test
  @DisplayName("Should not create personal record when same weight and less reps")
  void shouldNotCreatePersonalRecordWhenSameWeightAndLessReps() {
    Workout workout = createWorkout(1, List.of(createSet(8, 80.0)));

    MemberPersonalRecordEntity previousRecord = createPreviousRecord(80.0, 10);

    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 1))
      .thenReturn(List.of(previousRecord));

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).isEmpty();
    verify(jpaRepository, never()).save(any(MemberPersonalRecordEntity.class));
  }

  @Test
  @DisplayName("Should handle multiple exercises with mixed results")
  void shouldHandleMultipleExercisesWithMixedResults() {
    Workout.Exercise exercise1 = createExercise(1, List.of(createSet(12, 85.0)));
    Workout.Exercise exercise2 = createExercise(2, List.of(createSet(8, 60.0)));
    Workout.Exercise exercise3 = createExercise(3, List.of(createSet(15, 50.0)));

    Workout workout = Workout.builder()
      .exercises(List.of(exercise1, exercise2, exercise3))
      .build();

    MemberPersonalRecordEntity previousRecord1 = createPreviousRecord(80.0, 10);
    MemberPersonalRecordEntity previousRecord2 = createPreviousRecord(65.0, 10);
    MemberPersonalRecordEntity previousRecord3 = createPreviousRecord(50.0, 12);

    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 1))
      .thenReturn(List.of(previousRecord1));
    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 2))
      .thenReturn(List.of(previousRecord2));
    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 3))
      .thenReturn(List.of(previousRecord3));
    when(jpaRepository.save(any(MemberPersonalRecordEntity.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).hasSize(2);

    assertThat(results.getFirst().exerciseId()).isEqualTo(1);
    assertThat(results.getFirst().weight()).isEqualTo(85.0);
    assertThat(results.get(0).recordType()).isEqualTo(RecordType.WEIGHT);
    assertThat(results.get(0).deltaWeight()).isEqualTo(5.0);

    assertThat(results.get(1).exerciseId()).isEqualTo(3);
    assertThat(results.get(1).weight()).isEqualTo(50.0);
    assertThat(results.get(1).reps()).isEqualTo(15);
    assertThat(results.get(1).recordType()).isEqualTo(RecordType.REPS);
    assertThat(results.get(1).deltaReps()).isEqualTo(3);

    verify(jpaRepository, times(2)).save(any(MemberPersonalRecordEntity.class));
  }

  @Test
  @DisplayName("Should return empty list when workout has no exercises")
  void shouldReturnEmptyListWhenWorkoutHasNoExercises() {
    Workout workout = Workout.builder()
      .exercises(Collections.emptyList())
      .build();

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).isEmpty();
    verify(jpaRepository, never()).save(any(MemberPersonalRecordEntity.class));
  }

  @Test
  @DisplayName("Should skip exercise with no sets")
  void shouldSkipExerciseWithNoSets() {
    Workout.Exercise exercise = createExercise(1, Collections.emptyList());

    Workout workout = Workout.builder()
      .exercises(List.of(exercise))
      .build();

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).isEmpty();
    verify(jpaRepository, never()).save(any(MemberPersonalRecordEntity.class));
  }

  @Test
  @DisplayName("Should use best set from multiple sets")
  void shouldUseBestSetFromMultipleSets() {
    Workout.Exercise exercise = createExercise(1, List.of(
      createSet(8, 70.0),
      createSet(10, 80.0),
      createSet(6, 90.0)
    ));

    Workout workout = Workout.builder()
      .exercises(List.of(exercise))
      .build();

    MemberPersonalRecordEntity previousRecord = createPreviousRecord(85.0, 10);

    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 1))
      .thenReturn(List.of(previousRecord));
    when(jpaRepository.save(any(MemberPersonalRecordEntity.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().weight()).isEqualTo(90.0);
    assertThat(results.getFirst().reps()).isEqualTo(6);
    assertThat(results.getFirst().recordType()).isEqualTo(RecordType.WEIGHT);
    assertThat(results.getFirst().deltaWeight()).isEqualTo(5.0);
  }

  @Test
  @DisplayName("Should use best set by reps when weight is equal")
  void shouldUseBestSetByRepsWhenWeightIsEqual() {
    Workout.Exercise exercise = createExercise(1, List.of(
      createSet(8, 80.0),
      createSet(12, 80.0),
      createSet(10, 80.0)
    ));

    Workout workout = Workout.builder()
      .exercises(List.of(exercise))
      .build();

    MemberPersonalRecordEntity previousRecord = createPreviousRecord(80.0, 10);

    when(jpaRepository.findByMemberIdAndExerciseIdOrderByAchievedAtDesc(memberId, 1))
      .thenReturn(List.of(previousRecord));
    when(jpaRepository.save(any(MemberPersonalRecordEntity.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    List<PersonalRecordResult> results = personalRecordService.calculate(memberId, workout);

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().weight()).isEqualTo(80.0);
    assertThat(results.getFirst().reps()).isEqualTo(12);
    assertThat(results.getFirst().recordType()).isEqualTo(RecordType.REPS);
    assertThat(results.getFirst().deltaReps()).isEqualTo(2);
  }

  private Workout createWorkout(Integer exerciseId, List<Workout.Exercise.Set> sets) {
    Workout.Exercise exercise = createExercise(exerciseId, sets);
    return Workout.builder()
      .exercises(List.of(exercise))
      .build();
  }

  private Workout.Exercise createExercise(Integer id, List<Workout.Exercise.Set> sets) {
    return Workout.Exercise.builder()
      .id(id)
      .sets(sets)
      .build();
  }

  private Workout.Exercise.Set createSet(int reps, double weight) {
    return Workout.Exercise.Set.builder()
      .setNumber((short) 1)
      .reps((short) reps)
      .weight(weight)
      .build();
  }

  private MemberPersonalRecordEntity createPreviousRecord(double weight, int reps) {
    MemberPersonalRecordEntity record = new MemberPersonalRecordEntity();
    record.setWeight(weight);
    record.setReps((short) reps);
    return record;
  }
}
