package dev.jpitarch.ctrlgym.core.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PRProgress(LocalDate date, UUID exerciseId, double bestSet) {
}
