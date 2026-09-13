package dev.jpitarch.ctrlgym.core.dto;

import java.time.LocalDate;

public record WeeklyVisits(LocalDate week, int visits) {
}
