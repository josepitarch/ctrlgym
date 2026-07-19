package dev.jpitarch.ctrlgym.core.domain.exceptions;

import dev.jpitarch.ctrlgym.core.domain.Member;

public class DuplicateMembershipException extends RuntimeException {

  public DuplicateMembershipException(Member.Id memberId, String planId) {
    super("Member with id %s has already membership plan %s".formatted(memberId, planId));
  }

}
