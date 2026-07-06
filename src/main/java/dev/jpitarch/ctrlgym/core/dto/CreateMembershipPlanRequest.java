package dev.jpitarch.ctrlgym.core.dto;

import java.util.List;

public record CreateMembershipPlanRequest(String name, Double price, List<Integer> branches) {
}
