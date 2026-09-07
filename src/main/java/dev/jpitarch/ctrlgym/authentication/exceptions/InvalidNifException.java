package dev.jpitarch.ctrlgym.authentication.exceptions;

public class InvalidNifException extends RuntimeException {

  public InvalidNifException(String nif) {
    super("NIF %s is not valid or not identified by AEAT".formatted(nif));
  }

}
