package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonProperty("routine_id")
  private Integer routineId;

  @JsonProperty("day_number")
  private Integer dayNumber;

  @JsonProperty("started_at")
  private OffsetDateTime startedAt;

  @JsonProperty("finished_at")
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

      @JsonProperty("set_number")
      private Short setNumber;

      private Short reps;

      private double weight;

    }

  }

}
