package dev.jpitarch.ctrlgym.authentication.exceptions;

public class AccountNotActivatedException extends RuntimeException {
  public AccountNotActivatedException(String message) {
    super(message);
  }
}
