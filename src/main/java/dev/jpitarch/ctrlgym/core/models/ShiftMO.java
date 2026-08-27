package dev.jpitarch.ctrlgym.core.models;

import dev.jpitarch.ctrlgym.core.domain.enums.ShiftStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "employee_schedule_shift")
public class ShiftMO {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "series_id")
  private Long seriesId;

  @Column(name = "employee_id", nullable = false)
  private UUID employeeId;

  @Column(name = "gym_id", nullable = false)
  private Integer gymId;

  @Column(name = "gym_branch_id", nullable = false)
  private Integer gymBranchId;

  @Column(name = "shift_date", nullable = false)
  private LocalDate shiftDate;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @ColumnDefault("'SCHEDULED'")
  private ShiftStatus status;

  @Column(name = "is_exception", nullable = false)
  @ColumnDefault("false")
  private boolean isException;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "series_id", insertable = false, updatable = false)
  private ShiftSeriesMO series;
}
