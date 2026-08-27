package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateShiftRequest(
  @JsonProperty("shift_date") LocalDate shiftDate,
  @JsonProperty("start_time") LocalTime startTime,
  @JsonProperty("end_time") LocalTime endTime
) {}
