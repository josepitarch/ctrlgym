package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.MuscleGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Routine {

  private Integer id;

  private String name;

  private List<Day> days;

  @Data
  @Builder
  @AllArgsConstructor
  public static class Day {

    @JsonProperty("day_number")
    private Integer dayNumber;

    private String name;

    private String description;

    private List<Exercise> exercises;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Exercise implements Comparable<Exercise> {

      private Integer id;

      private String name;

      @JsonProperty("muscle_group")
      private MuscleGroup muscleGroup;

      private Integer position;

      private Integer sets;

      private Integer reps;

      @Override
      public int compareTo(Exercise o) {
        return this.name.compareTo(o.name);
      }


    }

  }

}
