package dev.jpitarch.ctrlgym.core.controllers.advices;

import dev.jpitarch.ctrlgym.core.domain.exceptions.*;
import dev.jpitarch.ctrlgym.core.events.ExceptionEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {

  private final ApplicationEventPublisher eventPublisher;

  @ExceptionHandler(MemberNotFoundException.class)
  public ProblemDetail handleMemberNotFoundException(MemberNotFoundException e, HttpServletRequest request) {
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problem.setTitle("Member Not Found");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());

    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.NOT_FOUND, request);

    return problem;
  }

  @ExceptionHandler(ExerciseNotFoundException.class)
  public ProblemDetail handleExerciseNotFoundException(ExerciseNotFoundException e, HttpServletRequest request) {
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problem.setTitle("Exercise Not Found");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());

    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.NOT_FOUND, request);

    return problem;
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ProblemDetail handleProductNotFoundException(ProductNotFoundException e, HttpServletRequest request) {
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problem.setTitle("Product Not Found");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());

    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.NOT_FOUND, request);

    return problem;
  }

  @ExceptionHandler(MemberWithoutAccessException.class)
  public ProblemDetail handleMemberWithoutAccessException(MemberWithoutAccessException e, HttpServletRequest request) {
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    problem.setTitle("Member Without Access");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());

    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.CONFLICT, request);

    return problem;
  }


  @ExceptionHandler(CoreBusinessException.class)
  public ProblemDetail handleCoreBusinessException(CoreBusinessException e, HttpServletRequest request) {
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    problem.setTitle("Unprocessable Entity");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());

    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.UNPROCESSABLE_CONTENT, request);

    return problem;
  }

  @ExceptionHandler(ManyPostalCodesException.class)
  public ProblemDetail handleManyPostalCodesException(ManyPostalCodesException e, HttpServletRequest request) {
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    problem.setTitle("Multiple Cities For Postal Code");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());

    publishExceptionEvent(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.CONFLICT, request);

    return problem;
  }

  @ExceptionHandler(AuthException.class)
  public ProblemDetail handleAuthException(AuthException e, HttpServletRequest request) {
    var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    problem.setTitle("Conflict");
    problem.setDetail(e.getSignup().name());
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
