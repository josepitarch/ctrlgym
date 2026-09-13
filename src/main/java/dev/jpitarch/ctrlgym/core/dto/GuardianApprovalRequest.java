package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record GuardianApprovalRequest(
  @JsonProperty("name") String name,
  @JsonProperty("first_surname") String firstSurname,
  @JsonProperty("second_surname") String secondSurname,
  @JsonProperty("accepted_document_version_ids") List<UUID> acceptedDocumentVersionIds
) {
}
