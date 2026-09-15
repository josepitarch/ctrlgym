package dev.jpitarch.ctrlgym.core.dto;

public record PersonalRecordResult(
  Integer exerciseId,
  double weight,
  int reps,
  RecordType recordType,
  double deltaWeight,
  int deltaReps
) {
  public enum RecordType {
    WEIGHT,
    REPS
  }
}
