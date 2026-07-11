package dev.jpitarch.ctrlgym.core.dto;

import java.time.YearMonth;
import java.util.Map;

public record RetentionVsChurn(Map<YearMonth, Double> retention, Map<YearMonth, Double> churn) {
}
