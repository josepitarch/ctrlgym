package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

public record MembershipSeniorityDistribution(@JsonValue Map<String, Integer> data) {
}
