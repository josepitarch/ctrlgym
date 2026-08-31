package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

  public static final Integer TAX = 21;

  private String id;

  private UUID memberId;

  private String name;

  @JsonProperty("first_surname")
  private String firstSurname;

  @JsonProperty("second_surname")
  private String secondSurname;

  private String nif;

  private String series;

  private String number;

  @JsonProperty("issue_at")
  private LocalDate issueAt;

  @JsonProperty("due_at")
  private LocalDate dueAt;

  private BigDecimal subtotal;

  private BigDecimal tax;

  private BigDecimal total;

  private String currency;

  private InvoiceStatus status;

  public String getFullName() {
    return name + " " + firstSurname + " " + secondSurname;
  }
}
