package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

public record TimeRange(@JsonProperty("opens_at") LocalTime opensAt, @JsonProperty("closes_at") LocalTime closesAt) {

  public static TimeRange of(LocalTime opensAt, LocalTime closesAt) {
    return new TimeRange(opensAt, closesAt);
  }
}
