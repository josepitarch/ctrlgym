package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateShiftRequest(
  @JsonProperty("employee_id") UUID employeeId,
  @JsonProperty("gym_id") Integer gymId,
  @JsonProperty("shift_date") LocalDate shiftDate,
  @JsonProperty("start_time") LocalTime startTime,
  @JsonProperty("end_time") LocalTime endTime
) {}
