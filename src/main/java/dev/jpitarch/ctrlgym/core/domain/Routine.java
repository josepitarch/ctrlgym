package dev.jpitarch.ctrlgym.core.domain;

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

      private String muscleGroup;

      private Integer position;

      private List<Set> sets;

      @Override
      public int compareTo(Exercise o) {
        return this.name.compareTo(o.name);
      }

      @Data
      @Builder
      @AllArgsConstructor
      public static class Set {

        private Integer number;

        private Integer repetition;

      }

    }

  }

}
