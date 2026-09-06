package dev.jpitarch.ctrlgym.core.domain.exceptions;

public class MembershipPlanNotFoundException extends RuntimeException {
  public MembershipPlanNotFoundException(String planId) {
    super("Membership plan with id %s does not exists".formatted(planId));
  }
}
