package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NextDaySuggestion(
  @JsonProperty("routine_id") Integer routineId,
  @JsonProperty("day_number") Integer dayNumber
) {

  public static NextDaySuggestion of(Integer routineId, Integer dayNumber) {
    return new NextDaySuggestion(routineId, dayNumber);
  }

}
