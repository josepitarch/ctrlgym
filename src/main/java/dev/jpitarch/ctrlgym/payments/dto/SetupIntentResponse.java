package dev.jpitarch.ctrlgym.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SetupIntentResponse(
  String id,
  @JsonProperty("client_secret") String clientSecret
) {
}
