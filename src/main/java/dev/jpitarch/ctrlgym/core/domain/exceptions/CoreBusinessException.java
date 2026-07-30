package dev.jpitarch.ctrlgym.core.domain.exceptions;

public class CoreBusinessException extends RuntimeException {
  public CoreBusinessException(Class<?> domainObject, String message) {
    super(message);
  }
}
