package dev.jpitarch.ctrlgym.authentication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record SignupRequest(
  String email,
  String password,
  String name,
  @JsonProperty("first_surname") String firstSurname,
  @Nullable @JsonProperty("second_surname") String secondSurname) {
}
