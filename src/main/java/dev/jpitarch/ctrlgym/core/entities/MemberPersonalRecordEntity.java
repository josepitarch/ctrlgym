package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "member_personal_records")
public class MemberPersonalRecordEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "member_id", nullable = false)
  private UUID memberId;

  @Column(name = "exercise_id", nullable = false)
  private Integer exerciseId;

  @Column(name = "weight", nullable = false)
  private Double weight;

  @Column(name = "reps", nullable = false)
  private Short reps;

  @ColumnDefault("now()")
  @Column(name = "achieved_at", nullable = false)
  private OffsetDateTime achievedAt;

  @Column(name = "workout_id")
  private Integer workoutId;
}
