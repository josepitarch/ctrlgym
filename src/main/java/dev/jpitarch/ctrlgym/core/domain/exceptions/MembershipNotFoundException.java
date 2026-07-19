package dev.jpitarch.ctrlgym.core.domain.exceptions;

import dev.jpitarch.ctrlgym.core.domain.Member;

public class MembershipNotFoundException extends RuntimeException {

  public MembershipNotFoundException(Member.Id memberId) {
    super("Member with id %s has no active membership".formatted(memberId));
  }

}
