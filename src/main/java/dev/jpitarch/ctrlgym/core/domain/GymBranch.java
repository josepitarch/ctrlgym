package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymBranch {

  private int id;

  private String name;

  private short capacity;

  private PeakHour peakHour;

  private Coordinates coordinates;

  public record PeakHour(LocalTime start, LocalTime end) {

  }

  public record Coordinates(Double latitude, Double longitude) {

  }
}
