package dev.jpitarch.ctrlgym.core.domain;

public record GymBranchId(int gymId, int branchId) {

  public static GymBranchId of(int gymId, int branchId) {
    return new GymBranchId(gymId, branchId);
  }

}
