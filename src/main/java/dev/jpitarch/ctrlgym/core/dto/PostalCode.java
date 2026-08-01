package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostalCode(
  @JsonProperty("postal_code") Integer postalCode,
  String city,
  String province,
  String state
) {
}
