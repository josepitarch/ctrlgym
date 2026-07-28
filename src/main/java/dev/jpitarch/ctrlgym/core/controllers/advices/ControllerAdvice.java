package dev.jpitarch.ctrlgym.core.controllers.advices;

import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberNotFoundException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberWithoutAccessException;
import dev.jpitarch.ctrlgym.core.events.ExceptionEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class ControllerAdvice {

  private final ApplicationEventPublisher eventPublisher;

  public ControllerAdvice(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @ExceptionHandler(MemberNotFoundException.class)
  public ProblemDetail handleMemberNotFoundException(MemberNotFoundException e, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problem.setTitle("Member Not Found");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());
    
    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.NOT_FOUND, request);
    
    return problem;
  }

  @ExceptionHandler(MemberWithoutAccessException.class)
  public ProblemDetail handleMemberWithoutAccessException(MemberWithoutAccessException e, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    problem.setTitle("Member Without Access");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());
    
    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.CONFLICT, request);
    
    return problem;
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ProblemDetail handleAuthorizationDeniedException(AuthorizationDeniedException e, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    problem.setTitle("Forbidden");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());
    
    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.FORBIDDEN, request);
    
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
