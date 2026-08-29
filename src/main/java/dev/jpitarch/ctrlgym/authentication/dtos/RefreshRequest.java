package dev.jpitarch.ctrlgym.authentication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshRequest(
  @JsonProperty("refresh_token") String refreshToken) {
}
