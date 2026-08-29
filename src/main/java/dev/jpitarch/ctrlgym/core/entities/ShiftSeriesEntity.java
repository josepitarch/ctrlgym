package dev.jpitarch.ctrlgym.core.entities;

import dev.jpitarch.ctrlgym.core.domain.enums.RecurrenceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "employee_schedule_shift_series")
public class ShiftSeriesEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "employee_id", nullable = false)
  private UUID employeeId;

  @Column(name = "gym_id", nullable = false)
  private Integer gymId;

  @Column(name = "gym_branch_id", nullable = false)
  private Integer gymBranchId;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "recurrence_type", nullable = false, length = 20)
  private RecurrenceType recurrenceType;

  @Column(name = "interval_value", nullable = false)
  @ColumnDefault("1")
  private Integer intervalValue;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "days_of_week", columnDefinition = "smallint[]")
  private List<Short> daysOfWeek;

  @Column(name = "series_start", nullable = false)
  private LocalDate seriesStart;

  @Column(name = "series_end")
  private LocalDate seriesEnd;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
