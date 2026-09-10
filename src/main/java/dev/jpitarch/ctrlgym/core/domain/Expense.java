package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

  private Long id;

  @JsonProperty("gym_branch_id")
  private Integer gymBranchId;

  private String concept;

  @JsonProperty("category_id")
  private Integer categoryId;

  private Type type;

  private Recurrence recurrence;

  private Double amount;

  @JsonProperty("expense_date")
  private LocalDate expenseDate;

  @JsonProperty("billing_day")
  private Short billingDay;

  @JsonProperty("estimated_amount")
  private Double estimatedAmount;

  private Boolean active;

  private Source source;

  public enum Type {
    FIXED,
    VARIABLE;

    public static Type from(String str) {
      return Type.valueOf(str.toUpperCase());
    }
  }

  public enum Recurrence {
    RECURRING,
    ONE_OFF;

    public static Recurrence from(String str) {
      return Recurrence.valueOf(str.toUpperCase());
    }
  }

  public enum Source {
    MANUAL,
    EXCEL;

    public static Source from(String str) {
      return Source.valueOf(str.toUpperCase());
    }
  }

}
