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
@SQLRestriction("is_active IS true")
public class ExpenseCategoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "gym_id", nullable = false)
  private Integer gymId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @ColumnDefault("now()")
  @Column(name = "created_at", nullable = false, insertable = false)
  private Instant createdAt;

  @ColumnDefault("true")
  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

}
