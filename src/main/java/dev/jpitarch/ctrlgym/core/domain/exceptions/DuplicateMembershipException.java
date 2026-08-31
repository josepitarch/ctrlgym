package dev.jpitarch.ctrlgym.core.domain.exceptions;

import java.util.UUID;

public class DuplicateMembershipException extends RuntimeException {

  public DuplicateMembershipException(UUID memberId, String planId) {
    super("Member with id %s has already membership plan %s".formatted(memberId, planId));
  }

}
