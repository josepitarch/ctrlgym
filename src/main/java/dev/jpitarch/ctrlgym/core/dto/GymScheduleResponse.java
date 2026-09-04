package dev.jpitarch.ctrlgym.core.dto;

import java.util.Map;

public record GymScheduleResponse(Map<Integer, TimeRange> schedule) {
}
