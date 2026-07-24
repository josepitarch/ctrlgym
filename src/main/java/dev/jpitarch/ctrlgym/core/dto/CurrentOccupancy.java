package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.GymBranch;

public record CurrentOccupancy(int count, int capacity, @JsonProperty("peak_hour") GymBranch.PeakHour peakHour) {

}
