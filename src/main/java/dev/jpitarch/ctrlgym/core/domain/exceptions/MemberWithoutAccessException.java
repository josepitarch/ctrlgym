package dev.jpitarch.ctrlgym.core.domain.exceptions;

import java.util.UUID;

public class MemberWithoutAccessException extends RuntimeException {

  public MemberWithoutAccessException(UUID memberId) {
    super("Member with id %s have no any active membership".formatted(memberId));
  }

}
