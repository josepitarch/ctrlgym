package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "expense_categories")
@SQLRestriction("is_active = true")
public class ExpenseCategoryEntity {

  @Id
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "code", nullable = false, length = 100)
  private String code;

  @ColumnDefault("now()")
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @ColumnDefault("true")
  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

}
