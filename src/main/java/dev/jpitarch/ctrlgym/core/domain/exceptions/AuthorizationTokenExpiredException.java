package dev.jpitarch.ctrlgym.core.domain.exceptions;

public class AuthorizationTokenExpiredException extends RuntimeException {
  public AuthorizationTokenExpiredException(String token) {
    super("Authorization token has expired: %s".formatted(token));
  }
}
