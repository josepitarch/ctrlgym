package dev.jpitarch.ctrlgym.core.domain.exceptions;

import java.util.UUID;

public class MembershipNotFoundException extends RuntimeException {

  public MembershipNotFoundException(UUID memberId) {
    super("Member with id %s has no active membership".formatted(memberId));
  }

}
