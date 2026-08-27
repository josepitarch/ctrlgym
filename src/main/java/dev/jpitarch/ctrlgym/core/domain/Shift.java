package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.ShiftStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {

  private Long id;

  @JsonProperty("series_id")
  private Long seriesId;

  @JsonProperty("employee_id")
  private UUID employeeId;

  @JsonProperty("gym_id")
  private Integer gymId;

  @JsonProperty("gym_branch_id")
  private Integer gymBranchId;

  @JsonProperty("shift_date")
  private LocalDate shiftDate;

  @JsonProperty("start_time")
  private LocalTime startTime;

  @JsonProperty("end_time")
  private LocalTime endTime;

  private ShiftStatus status;

  @JsonProperty("is_exception")
  private boolean isException;

}
