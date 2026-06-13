package dev.jpitarch.ctrlgym.core.domain;

import dev.jpitarch.ctrlgym.core.domain.enums.WorkoutStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class Workout {

  private Integer routineId;

  private Integer dayNumber;

  private OffsetDateTime startedAt;

  private OffsetDateTime finishedAt;

  private WorkoutStatus status;

  private List<Exercise> exercises;

  @Data
  @Builder
  @AllArgsConstructor
  public static class Exercise {

    private Integer id;

    private List<Set> sets;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Set {

      private Short setNumber;

      private Short reps;

    }

  }

}
