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

  @Column(name = "category_id", nullable = false)
  private Integer categoryId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "nature", columnDefinition = "expense_nature not null", nullable = false)
  private ExpenseNature nature;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "frequency", columnDefinition = "expense_frequency not null", nullable = false)
  private ExpenseFrequency frequency;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "recurrence_period", columnDefinition = "recurrence_period")
  private RecurrencePeriod recurrencePeriod;

  @Column(name = "expected_amount")
  private BigDecimal expectedAmount;

  @Column(name = "currency_code", nullable = false, length = 3)
  @ColumnDefault("'EUR'")
  private String currencyCode;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", columnDefinition = "expense_status not null", nullable = false)
  @ColumnDefault("'ACTIVE'")
  private ExpenseStatus status;

  @ColumnDefault("now()")
  @Column(name = "created_at", nullable = false, insertable = false)
  private Instant createdAt;

  @ColumnDefault("now()")
  @Column(name = "updated_at", nullable = false, insertable = false)
  private Instant updatedAt;

  public enum ExpenseNature {
    FIXED, VARIABLE
  }

  public enum ExpenseFrequency {
    ONE_TIME, RECURRING
  }

  public enum RecurrencePeriod {
    DAILY, WEEKLY, MONTHLY, YEARLY
  }

  public enum ExpenseStatus {
    ACTIVE, INACTIVE
  }

}
