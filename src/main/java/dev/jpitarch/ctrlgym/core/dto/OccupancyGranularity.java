package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.Granularity;

import java.time.LocalDateTime;
import java.util.List;

public record OccupancyGranularity(Granularity granularity, @JsonProperty("data_points") List<OccupancyDataPoint> dataPoints) {
  public record OccupancyDataPoint(LocalDateTime timestamp, int count) {
  }
}
