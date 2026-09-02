package dev.jpitarch.ctrlgym.core.domain.exceptions;

public class OrderNotFoundException extends RuntimeException {

  public OrderNotFoundException(Integer orderId) {
    super("Order with id %s does not exists".formatted(orderId));
  }

}
