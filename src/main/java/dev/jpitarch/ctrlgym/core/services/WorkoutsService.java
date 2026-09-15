package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.domain.enums.WorkoutStatus;
import dev.jpitarch.ctrlgym.core.dto.NextDaySuggestion;
import dev.jpitarch.ctrlgym.core.dto.PersonalRecordResult;
import dev.jpitarch.ctrlgym.core.dto.WorkoutSummary;
import dev.jpitarch.ctrlgym.core.repositories.RoutinesRepository;
import dev.jpitarch.ctrlgym.core.repositories.WorkoutsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutsService {

  private final WorkoutsRepository workoutsRepository;

  private final RoutinesRepository routinesRepository;

  private final PersonalRecordService personalRecordService;

  @Transactional
  public Workout create(Workout workout, UUID memberId) {
    log.info("Creating workout for member with id {}...", memberId);
    Workout savedWorkout = workoutsRepository.save(workout, memberId);
    List<PersonalRecordResult> result = personalRecordService.calculate(memberId, workout);
    WorkoutSummary summary = WorkoutSummaryCalculator.calculate(workout);
    return savedWorkout;
  }

  public Page<Workout> getWorkouts(UUID memberId, Optional<Integer> routineId, Pageable pageable) {
    return routineId
      .map(id -> workoutsRepository.findByMemberIdAndRoutineId(memberId, id, pageable))
      .orElseGet(() -> workoutsRepository.findByMemberId(memberId, pageable));
  }

  public Optional<Workout> findLastCompleted(UUID memberId) {
    return workoutsRepository.findLastCompleted(memberId);
  }

  public Optional<NextDaySuggestion> getNextDaySuggestion(UUID memberId) {
    List<Routine> routines = routinesRepository.findByMemberId(memberId);
    if (routines.isEmpty()) return Optional.empty();

    Optional<Workout> lastWorkout = workoutsRepository.findLastCompleted(memberId);

    if (lastWorkout.isEmpty()) {
      Routine firstRoutine = routines.get(0);
      return Optional.of(NextDaySuggestion.of(firstRoutine.getId(), firstRoutine.getDays().get(0).getDayNumber()));
    }

    Workout last = lastWorkout.get();
    Routine routine = routines.stream()
      .filter(r -> r.getId().equals(last.getRoutineId()))
      .findFirst()
      .orElse(routines.get(0));

    List<Routine.Day> days = routine.getDays();
    int lastIndex = -1;
    for (int i = 0; i < days.size(); i++) {
      if (days.get(i).getDayNumber().equals(last.getDayNumber())) {
        lastIndex = i;
        break;
      }
    }

    int nextIndex = (lastIndex >= 0) ? (lastIndex + 1) % days.size() : 0;
    Routine.Day nextDay = days.get(nextIndex);

    return Optional.of(NextDaySuggestion.of(routine.getId(), nextDay.getDayNumber()));
  }

  public static class WorkoutSummaryCalculator {

    public static WorkoutSummary calculate(Workout workout) {
      long durationMinutes = Duration.between(workout.getStartedAt(), workout.getFinishedAt()).toMinutes();

      BigDecimal totalVolumeKg = BigDecimal.ZERO;
      int totalSets = 0;
      List<WorkoutSummary.ExerciseResult> exerciseResults = new java.util.ArrayList<>();

      for (Workout.Exercise exercise : workout.getExercises()) {
        BigDecimal exerciseVolume = BigDecimal.ZERO;
        double maxWeight = 0;
        int maxReps = 0;

        for (Workout.Exercise.Set set : exercise.getSets()) {
          BigDecimal setVolume = BigDecimal.valueOf(set.getWeight()).multiply(BigDecimal.valueOf(set.getReps()));
          exerciseVolume = exerciseVolume.add(setVolume);
          totalVolumeKg = totalVolumeKg.add(setVolume);
          totalSets++;

          if (set.getWeight() > maxWeight) {
            maxWeight = set.getWeight();
            maxReps = set.getReps();
          } else if (set.getWeight() == maxWeight && set.getReps() > maxReps) {
            maxReps = set.getReps();
          }
        }

        exerciseResults.add(new WorkoutSummary.ExerciseResult(
          exercise.getId().longValue(),
          exerciseVolume,
          BigDecimal.valueOf(maxWeight),
          maxReps
        ));
      }

      return new WorkoutSummary(
        durationMinutes,
        totalVolumeKg,
        workout.getExercises().size(),
        totalSets,
        exerciseResults
      );
    }
  }

  @Component
  @RequiredArgsConstructor
  public static class WorkoutMessageTypeResolver {

    private static final int INACTIVITY_DAYS_FOR_COMEBACK = 14;

    private static final Set<Integer> STREAK_MILESTONES = Set.of(3, 7, 14, 30, 50, 100);

    private final WorkoutsRepository workoutsRepository;

    public Workout.MessageType resolve(UUID memberId, Workout workout, WorkoutSummary summary, List<PersonalRecordResult> personalRecords, int currentStreak) {
      if (isFirstWorkout(memberId)) return Workout.MessageType.FIRST_WORKOUT;
      if (hasPersonalRecord(personalRecords)) return Workout.MessageType.PERSONAL_RECORD;
      if (isComeback(memberId, workout)) return Workout.MessageType.COMEBACK;
      if (STREAK_MILESTONES.contains(currentStreak)) return Workout.MessageType.STREAK_MILESTONE;
      if (isHighVolume(memberId, summary)) return Workout.MessageType.HIGH_VOLUME;

      return Workout.MessageType.STANDARD;
    }

    private boolean isFirstWorkout(UUID memberId) {
      return workoutsRepository.countByMemberIdAndStatus(memberId, WorkoutStatus.COMPLETED) == 0;
      // ojo: esta llamada se hace ANTES de guardar la sesión actual como completada
    }

    private boolean hasPersonalRecord(List<PersonalRecordResult> personalRecords) {
      return personalRecords != null && !personalRecords.isEmpty();
    }

    private boolean isComeback(UUID memberId, Workout currentWorkout) {
      return workoutsRepository.findPreviousSession(memberId, currentWorkout.getId())
        .map(previous -> {
          long daysSince = Duration.between(previous.getFinishedAt(), currentWorkout.getFinishedAt()).toDays();
          return daysSince >= INACTIVITY_DAYS_FOR_COMEBACK;
        })
        .orElse(false);
    }

    private boolean isHighVolume(UUID memberId, WorkoutSummary summary) {
      BigDecimal avgVolume = workoutsRepository.getAverageVolumeLast30Days(memberId);
      if (avgVolume == null || avgVolume.compareTo(BigDecimal.ZERO) == 0) return false;

      // más de un 30% por encima de su media
      BigDecimal threshold = avgVolume.multiply(BigDecimal.valueOf(1.3));
      return summary.totalVolumeKg().compareTo(threshold) > 0;
    }
  }

}
