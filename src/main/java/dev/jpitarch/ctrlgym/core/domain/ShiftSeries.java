package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.RecurrenceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftSeries {
  private Long id;

  @JsonProperty("employee_id")
  private UUID employeeId;

  @JsonProperty("gym_id")
  private Integer gymId;

  @JsonProperty("gym_branch_id")
  private Integer gymBranchId;

  @JsonProperty("start_time")
  private LocalTime startTime;

  @JsonProperty("end_time")
  private LocalTime endTime;

  @JsonProperty("recurrence_type")
  private RecurrenceType recurrenceType;

  @JsonProperty("interval_value")
  private Integer intervalValue;

  @JsonProperty("days_of_week")
  private List<Short> daysOfWeek;

  @JsonProperty("series_start")
  private LocalDate seriesStart;

  @JsonProperty("series_end")
  private LocalDate seriesEnd;
}
