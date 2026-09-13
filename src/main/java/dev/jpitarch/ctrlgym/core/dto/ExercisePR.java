package dev.jpitarch.ctrlgym.core.dto;

import java.util.UUID;

public record ExercisePR(UUID exerciseId, double prWeight, int prReps) {
}
