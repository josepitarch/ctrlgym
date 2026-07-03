package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.GymBranch;

import java.time.LocalTime;

public record CurrentOccupancy(int count, int capacity, @JsonProperty("peak_hour") GymBranch.PeakHour peakHour) {

}
