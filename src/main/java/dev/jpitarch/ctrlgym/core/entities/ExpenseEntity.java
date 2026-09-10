package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "expenses")
public class ExpenseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "gym_branch_id", nullable = false)
  private Integer gymBranchId;

  @Column(name = "concept", nullable = false)
  private String concept;

  @Column(name = "category_id", nullable = false)
  private Integer categoryId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "type", columnDefinition = "expense_type not null", nullable = false)
  private ExpenseType type;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "recurrence", columnDefinition = "expense_recurrence not null", nullable = false)
  private ExpenseRecurrence recurrence;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "expense_date")
  private LocalDate expenseDate;

  @Column(name = "billing_day")
  private Short billingDay;

  @Column(name = "estimated_amount")
  private BigDecimal estimatedAmount;

  @Column(name = "active", nullable = false)
  @ColumnDefault("true")
  private Boolean active;

  @Column(name = "source", nullable = false, length = 10)
  private String source;

  @ColumnDefault("now()")
  @Column(name = "created_at", nullable = false, insertable = false)
  private Instant createdAt;

  @ColumnDefault("now()")
  @Column(name = "updated_at", nullable = false, insertable = false)
  private Instant updatedAt;

  public enum ExpenseType {
    FIXED, VARIABLE
  }

  public enum ExpenseRecurrence {
    ONE_OFF, RECURRING
  }

}
