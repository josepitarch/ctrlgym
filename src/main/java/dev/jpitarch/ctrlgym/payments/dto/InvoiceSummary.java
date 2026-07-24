package dev.jpitarch.ctrlgym.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceSummary(
  @JsonProperty("id") String id,
  @JsonProperty("issue_at") LocalDate issueAt,
  @JsonProperty("paid_at") LocalDate paidAt,
  @JsonProperty("total") BigDecimal total
) {
}
