package dev.jpitarch.ctrlgym.core.domain;

public record GymBranchId(Integer gymId, Integer branchId) {

  public static GymBranchId of(Integer gymId, Integer branchId) {
    return new GymBranchId(gymId, branchId);
  }

}
