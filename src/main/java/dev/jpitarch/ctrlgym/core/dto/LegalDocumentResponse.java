package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;

import java.time.LocalDate;
import java.util.UUID;

public record LegalDocumentResponse(
  @JsonProperty("document_version_id") UUID documentVersionId,
  @JsonProperty("type") LegalDocumentType type,
  @JsonProperty("version") String version,
  @JsonProperty("content") String content,
  @JsonProperty("effective_date") LocalDate effectiveDate
) {
}
