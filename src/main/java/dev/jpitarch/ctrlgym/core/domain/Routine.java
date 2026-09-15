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

  public Day getNextDay(Integer dayNumber) {
    int lastIndex = -1;
    for (int i = 0; i < days.size(); i++) {
      if (days.get(i).getDayNumber().equals(dayNumber)) {
        lastIndex = i;
        break;
      }
    }
    int nextIndex = (lastIndex >= 0) ? (lastIndex + 1) % days.size() : 0;
    return days.get(nextIndex);
  }

  @Data
  @Builder
  @AllArgsConstructor
  public static class Day {

    @JsonProperty("day_number")
    private Integer dayNumber;

    private String name;

    private List<Exercise> exercises;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Exercise implements Comparable<Exercise> {

      private Integer id;

      private String name;

      @JsonProperty("muscle_group")
      private MuscleGroup muscleGroup;

      private String image;

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

        private Short number;

        private Short repetition;

      }

    }

  }

}
