package dev.jpitarch.ctrlgym.core.dto;

import java.time.YearMonth;
import java.util.Map;

public record CashFlow(Map<YearMonth, Double> expenses, Map<YearMonth, Double> revenues) {
}
