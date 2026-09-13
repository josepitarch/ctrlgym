package dev.jpitarch.ctrlgym.core.dto;

import java.time.LocalDate;

public record StreakInfo(LocalDate lastDay, int streak) {
}
