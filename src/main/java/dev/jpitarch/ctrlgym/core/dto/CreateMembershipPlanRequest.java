package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateMembershipPlanRequest(String name, Double price, Integer branch, @JsonProperty("all_branches") boolean allBranches) {
}
