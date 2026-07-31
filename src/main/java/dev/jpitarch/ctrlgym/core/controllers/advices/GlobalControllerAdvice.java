package dev.jpitarch.ctrlgym.core.controllers.advices;

import dev.jpitarch.ctrlgym.core.events.ExceptionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

  private final ApplicationEventPublisher eventPublisher;

  public GlobalControllerAdvice(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
    log.error("Invalid argument: {}", e.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problem.setTitle("Bad Request");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());

    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.BAD_REQUEST, request);

    return problem;
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ProblemDetail handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
    log.error("Access Denied: {}", e.getMessage());
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    problem.setTitle("Forbidden");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());

    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.FORBIDDEN, request);

    return problem;
  }


  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(Exception e, HttpServletRequest request) {
    log.error("Unexpected error: {}", e.getMessage(), e);
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    problem.setTitle("Internal Server Error");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());

    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);

    return problem;
  }

  private void publishExceptionEvent(String exceptionType, String message, HttpStatus status, HttpServletRequest request) {
    eventPublisher.publishEvent(new ExceptionEvent(
      this,
      exceptionType,
      message,
      status,
      request.getRequestURI(),
      request.getMethod()
    ));
  }
}
