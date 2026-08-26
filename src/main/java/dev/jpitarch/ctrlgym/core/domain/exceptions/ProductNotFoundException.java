package dev.jpitarch.ctrlgym.core.domain.exceptions;

public class ProductNotFoundException extends RuntimeException {

  public ProductNotFoundException(Integer productId) {
    super("Product with id %s does not exists".formatted(productId));
  }

}
