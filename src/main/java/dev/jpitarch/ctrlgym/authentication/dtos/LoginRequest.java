package dev.jpitarch.ctrlgym.authentication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

public record LoginRequest(
  String email,
  String password,
  @Nullable @JsonProperty("gym_id") Integer gymId
) {}
