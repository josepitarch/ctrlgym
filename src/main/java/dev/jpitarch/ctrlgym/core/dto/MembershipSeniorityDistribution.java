package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public record MembershipSeniorityDistribution(@JsonValue List<Object[]> data) {
}
