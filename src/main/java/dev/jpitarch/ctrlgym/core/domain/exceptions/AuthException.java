package dev.jpitarch.ctrlgym.core.domain.exceptions;

public class AuthException extends RuntimeException {

  public AuthException(Signup signup, Integer gymId, String email) {
    super("[SIGNUP]: %s exception produced by user with email %s of gym with id %s".formatted(signup.name(), email, gymId));
  }

  public enum Signup {
    IS_IN_MIGRATION,
    ANOTHER_GYM,
    ALREADY_EXISTS
  }

}
