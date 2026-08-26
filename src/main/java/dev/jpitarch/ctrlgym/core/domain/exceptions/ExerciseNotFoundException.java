package dev.jpitarch.ctrlgym.core.domain.exceptions;

public class ExerciseNotFoundException extends RuntimeException {

  public ExerciseNotFoundException(Integer exerciseId) {
    super("Exercise with id %s does not exists".formatted(exerciseId));
  }

}
