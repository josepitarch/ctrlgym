package dev.jpitarch.ctrlgym.core.dto;

import java.time.LocalTime;

public record TimeRange(LocalTime opensAt, LocalTime closesAt) {
}
