package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.GuardianConsentStatus;

import java.time.LocalDate;

public record GuardianAuthorizationDto(
  @JsonProperty("member_first_name") String memberFirstName,
  @JsonProperty("member_last_name") String memberLastName,
  @JsonProperty("member_date_of_birth") LocalDate memberDateOfBirth,
  @JsonProperty("gym_name") String gymName,
  @JsonProperty("authorization_text") String authorizationText,
  @JsonProperty("requires_accompaniment") boolean requiresAccompaniment,
  GuardianConsentStatus status
) {
}
