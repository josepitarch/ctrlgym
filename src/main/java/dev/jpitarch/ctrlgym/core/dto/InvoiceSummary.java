package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceSummary(
  @JsonProperty("id") String id,
  @JsonProperty("issue_at") LocalDate issueAt,
  @JsonProperty("total") BigDecimal total,
  InvoiceStatus status
) {
}
