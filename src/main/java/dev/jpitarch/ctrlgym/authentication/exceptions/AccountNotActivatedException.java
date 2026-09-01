package dev.jpitarch.ctrlgym.authentication.exceptions;

import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;

import java.util.UUID;

public class AccountNotActivatedException extends RuntimeException {
  public AccountNotActivatedException(UUID memberId, UserStatus status) {
    super("Member with id %s is not active. Current status: %s".formatted(memberId, status));
  }
}
