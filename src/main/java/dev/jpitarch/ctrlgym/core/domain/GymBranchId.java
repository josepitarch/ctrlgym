package dev.jpitarch.ctrlgym.core.domain;

import org.jspecify.annotations.NonNull;

public record GymBranchId(Integer gymId, Integer branchId) {

  public static GymBranchId of(Integer gymId, Integer branchId) {
    return new GymBranchId(gymId, branchId);
  }

  public @NonNull String toString() {
    return gymId + "-" + branchId;
  }

}
