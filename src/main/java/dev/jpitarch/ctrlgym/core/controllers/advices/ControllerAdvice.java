package dev.jpitarch.ctrlgym.core.controllers.advices;

import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberNotFoundException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberWithoutAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

  @ExceptionHandler(MemberNotFoundException.class)
  public ResponseEntity<?> handleMemberNotFoundException() {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(MemberWithoutAccessException.class)
  public ResponseEntity<?> handleMemberWithoutAccessException() {
    return new ResponseEntity<>(HttpStatus.CONFLICT);
  }

}
