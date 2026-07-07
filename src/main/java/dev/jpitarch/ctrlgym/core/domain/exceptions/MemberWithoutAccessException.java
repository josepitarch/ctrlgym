package dev.jpitarch.ctrlgym.core.domain.exceptions;

import dev.jpitarch.ctrlgym.core.domain.Member;

public class MemberWithoutAccessException extends RuntimeException {

  public MemberWithoutAccessException(Member.Id memberId) {
    super("Member with id %s have no any active membership".formatted(memberId));
  }

}
