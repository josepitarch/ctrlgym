package dev.jpitarch.ctrlgym.payments.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SetupIntentResponse(
  String id,
  @JsonProperty("client_secret") String clientSecret
) {
}
