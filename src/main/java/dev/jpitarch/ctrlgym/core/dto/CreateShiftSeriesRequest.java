package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.RecurrenceType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateShiftSeriesRequest(
  @JsonProperty("employee_id") UUID employeeId,
  @JsonProperty("gym_id") Integer gymId,
  @JsonProperty("start_time") LocalTime startTime,
  @JsonProperty("end_time") LocalTime endTime,
  @JsonProperty("recurrence_type") RecurrenceType recurrenceType,
  @JsonProperty("interval_value") Integer intervalValue,
  @JsonProperty("days_of_week") List<Short> daysOfWeek,
  @JsonProperty("series_start") LocalDate seriesStart,
  @JsonProperty("series_end") LocalDate seriesEnd
) {}
