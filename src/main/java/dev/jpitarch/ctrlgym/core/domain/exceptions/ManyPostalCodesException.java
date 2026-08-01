package dev.jpitarch.ctrlgym.core.domain.exceptions;

public class ManyPostalCodesException extends RuntimeException {

  public ManyPostalCodesException(Integer postalCode) {
    super("Multiple cities found for postal code %d".formatted(postalCode));
  }

}
