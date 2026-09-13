package dev.jpitarch.ctrlgym.authentication.exceptions;

public class DuplicateEmailException extends RuntimeException {

  public DuplicateEmailException(String email) {
    super("Email '%s' is already registered in this gym".formatted(email));
  }

}
