package dev.jpitarch.ctrlgym.core.dto;

import java.math.BigDecimal;
import java.util.List;

public record WorkoutSummary(
  long durationMinutes,
  BigDecimal totalVolumeKg,
  int exercisesCompleted,
  int totalSets,
  List<ExerciseResult> exerciseResults
) {

  public record ExerciseResult(
    Long exerciseId,
    BigDecimal volumeKg,
    BigDecimal maxWeightKg,
    Integer maxReps
  ) {
  }
}
