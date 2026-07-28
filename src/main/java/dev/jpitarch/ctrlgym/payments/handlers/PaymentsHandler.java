package dev.jpitarch.ctrlgym.payments.handlers;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.events.ExceptionEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice(basePackages = "dev.jpitarch.ctrlgym.payments.controllers")
public class PaymentsHandler {

  private final ApplicationEventPublisher eventPublisher;

  public PaymentsHandler(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @ExceptionHandler(StripeException.class)
  public ProblemDetail handleStripeException(StripeException e, HttpServletRequest request) {
    HttpStatus status = HttpStatus.valueOf(e.getStatusCode());
    
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
    problem.setTitle("Payment Error");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());
    
    eventPublisher.publishEvent(new ExceptionEvent(
      this,
      e.getClass().getSimpleName(),
      e.getMessage(),
      status,
      request.getRequestURI(),
      request.getMethod()
    ));
    
    return problem;
  }

}
