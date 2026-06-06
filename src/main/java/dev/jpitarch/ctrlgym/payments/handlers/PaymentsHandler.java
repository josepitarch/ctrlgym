package dev.jpitarch.ctrlgym.payments.handlers;

import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "dev.jpitarch.ctrlgym.payments.controllers")
public class PaymentsHandler {

  @ExceptionHandler(StripeException.class)
  public ResponseEntity<String> handleStripeException(StripeException e) {
    return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
  }

}
