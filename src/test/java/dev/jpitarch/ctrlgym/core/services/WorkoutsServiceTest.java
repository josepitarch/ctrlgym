package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.repositories.WorkoutsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutsServiceTest {

  @InjectMocks
  WorkoutsService workoutsService;

  @Mock
  WorkoutsRepository workoutsRepository;

  @Mock
  RoutinesService routinesService;

  final UUID memberId = UUID.randomUUID();

  @Test
  @DisplayName("Should return empty when member has no routines")
  void shouldReturnEmptyWhenNoRoutines() {
    when(routinesService.getRoutines(memberId)).thenReturn(Collections.emptyList());

    Optional<Routine.Day> result = workoutsService.getNextDaySuggestion(memberId);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should suggest first day of first routine when no completed workouts")
  void shouldSuggestFirstDayWhenNoWorkouts() {
    Routine routine = createRoutine(1, "Push", List.of(1, 2, 3));
    when(routinesService.getRoutines(memberId)).thenReturn(List.of(routine));
    when(workoutsRepository.findLastCompleted(memberId)).thenReturn(Optional.empty());

    Optional<Routine.Day> result = workoutsService.getNextDaySuggestion(memberId);

    assertThat(result).isPresent();
    assertThat(result.get().getDayNumber()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should suggest next day after last completed workout")
  void shouldSuggestNextDayAfterLastWorkout() {
    Routine routine = createRoutine(1, "Push", List.of(1, 2, 3));
    Workout lastWorkout = Workout.builder().routineId(1).dayNumber(1).build();

    when(routinesService.getRoutines(memberId)).thenReturn(List.of(routine));
    when(workoutsRepository.findLastCompleted(memberId)).thenReturn(Optional.of(lastWorkout));

    Optional<Routine.Day> result = workoutsService.getNextDaySuggestion(memberId);

    assertThat(result).isPresent();
    assertThat(result.get().getDayNumber()).isEqualTo(2);
  }

  @Test
  @DisplayName("Should wrap around to first day after last day")
  void shouldWrapAroundToFirstDay() {
    Routine routine = createRoutine(1, "Push", List.of(1, 2, 3));
    Workout lastWorkout = Workout.builder().routineId(1).dayNumber(3).build();

    when(routinesService.getRoutines(memberId)).thenReturn(List.of(routine));
    when(workoutsRepository.findLastCompleted(memberId)).thenReturn(Optional.of(lastWorkout));

    Optional<Routine.Day> result = workoutsService.getNextDaySuggestion(memberId);

    assertThat(result).isPresent();
    assertThat(result.get().getDayNumber()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should suggest same day when routine has only one day")
  void shouldSuggestSameDayWhenSingleDayRoutine() {
    Routine routine = createRoutine(1, "Full Body", List.of(1));
    Workout lastWorkout = Workout.builder().routineId(1).dayNumber(1).build();

    when(routinesService.getRoutines(memberId)).thenReturn(List.of(routine));
    when(workoutsRepository.findLastCompleted(memberId)).thenReturn(Optional.of(lastWorkout));

    Optional<Routine.Day> result = workoutsService.getNextDaySuggestion(memberId);

    assertThat(result).isPresent();
    assertThat(result.get().getDayNumber()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should use routine from last workout when multiple routines exist")
  void shouldUseRoutineFromLastWorkout() {
    Routine routine1 = createRoutine(1, "Push", List.of(1, 2, 3));
    Routine routine2 = createRoutine(2, "Pull", List.of(1, 2));
    Workout lastWorkout = Workout.builder().routineId(2).dayNumber(2).build();

    when(routinesService.getRoutines(memberId)).thenReturn(List.of(routine1, routine2));
    when(workoutsRepository.findLastCompleted(memberId)).thenReturn(Optional.of(lastWorkout));

    Optional<Routine.Day> result = workoutsService.getNextDaySuggestion(memberId);

    assertThat(result).isPresent();
    assertThat(result.get().getDayNumber()).isEqualTo(1);
  }

  private Routine createRoutine(Integer id, String name, List<Integer> dayNumbers) {
    List<Routine.Day> days = dayNumbers.stream()
      .map(dayNum -> Routine.Day.builder().dayNumber(dayNum).name("Day " + dayNum).build())
      .toList();

    return Routine.builder()
      .id(id)
      .name(name)
      .days(days)
      .build();
  }
}
