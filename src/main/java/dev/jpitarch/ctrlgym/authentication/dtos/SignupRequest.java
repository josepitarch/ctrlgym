package dev.jpitarch.ctrlgym.authentication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SignupRequest(
  String email,
  String password,
  String name,
  @JsonProperty("first_surname") String firstSurname,
  @Nullable @JsonProperty("second_surname") String secondSurname,
  @Nullable String gender,
  @Nullable @JsonProperty("birth_date") LocalDate birthDate,
  @Nullable @JsonProperty("accepted_document_version_ids") List<UUID> acceptedDocumentVersionIds) {
}
