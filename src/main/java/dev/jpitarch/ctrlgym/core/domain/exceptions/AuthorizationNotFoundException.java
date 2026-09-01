package dev.jpitarch.ctrlgym.core.domain.exceptions;

public class AuthorizationNotFoundException extends RuntimeException {
  public AuthorizationNotFoundException(String token) {
    super("Authorization not found for token: %s".formatted(token));
  }
}
