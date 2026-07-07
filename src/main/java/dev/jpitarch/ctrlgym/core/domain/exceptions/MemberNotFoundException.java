package dev.jpitarch.ctrlgym.core.domain.exceptions;

import dev.jpitarch.ctrlgym.core.domain.Member;

public class MemberNotFoundException extends RuntimeException {

  public MemberNotFoundException(Member.Id memberId) {
    super("Member with id %s does not exists".formatted(memberId));
  }

}
