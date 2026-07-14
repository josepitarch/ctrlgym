package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Heartbeat(double rate, @JsonProperty("cpu_percent") double cpuPercent, double temperature) {
}
