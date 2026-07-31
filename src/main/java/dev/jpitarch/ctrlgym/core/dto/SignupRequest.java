package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record SignupRequest(
  String email,
  String password,
  @JsonProperty("gym_id") Integer gymId,
  String name,
  @JsonProperty("first_surname") String firstSurname,
  @Nullable @JsonProperty("second_surname") String secondSurname) {
}
