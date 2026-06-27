package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

  public static final Integer TAX = 21;

  private String id;

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

  private BigDecimal subtotal;

  private BigDecimal tax;

  private BigDecimal total;

  public String getFullName() {
    return name + " " + firstSurname + " " + secondSurname;
  }
}
