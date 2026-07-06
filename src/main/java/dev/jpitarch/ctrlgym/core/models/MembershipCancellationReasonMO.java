package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "membership_cancellation_reasons")
public class MembershipCancellationReasonMO {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

  @ColumnDefault("true")
  @Column(name = "active", nullable = false)
  private Boolean active;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;


}
