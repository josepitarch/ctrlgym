package dev.jpitarch.ctrlgym.authentication.exceptions;

public class MissingGuardianEmailException extends RuntimeException {

  public MissingGuardianEmailException() {
    super("Guardian email is required for minor signup");
  }

}
