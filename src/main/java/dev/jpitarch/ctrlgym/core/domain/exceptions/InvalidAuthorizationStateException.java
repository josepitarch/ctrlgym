package dev.jpitarch.ctrlgym.core.domain.exceptions;

import dev.jpitarch.ctrlgym.core.domain.enums.GuardianConsentStatus;

public class InvalidAuthorizationStateException extends RuntimeException {
  public InvalidAuthorizationStateException(GuardianConsentStatus status) {
    super("Authorization is in invalid state: %s".formatted(status));
  }
}
