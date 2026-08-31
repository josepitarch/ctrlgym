package dev.jpitarch.ctrlgym.core.domain.exceptions;

import java.util.UUID;

public class MemberNotFoundException extends RuntimeException {

  public MemberNotFoundException(UUID memberId) {
    super("Member with id %s does not exists".formatted(memberId));
  }

}
