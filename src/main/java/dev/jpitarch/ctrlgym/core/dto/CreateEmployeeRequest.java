package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;

public record CreateEmployeeRequest(
  String name,
  @JsonProperty("first_surname") String firstSurname,
  @JsonProperty("second_surname") String secondSurname,
  String email,
  Gender gender,
  @JsonProperty("gym_branch_id") Integer gymBranchId,
  @JsonProperty("all_branches") boolean allBranches
) {}
