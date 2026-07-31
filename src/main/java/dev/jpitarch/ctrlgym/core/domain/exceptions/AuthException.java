package dev.jpitarch.ctrlgym.core.domain.exceptions;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

  private final Signup signup;

  public AuthException(Signup signup, Integer gymId, String email) {
    this.signup = signup;
    super("[SIGNUP]: %s exception produced by user with email %s of gym with id %s".formatted(signup.name(), email, gymId));
  }

  public enum Signup {
    IS_IN_MIGRATION,
    ANOTHER_GYM,
    ALREADY_EXISTS
  }

}
