package dev.jpitarch.ctrlgym.core.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;

@Getter
public class ExceptionEvent extends ApplicationEvent {

  private final String exceptionType;
  private final String message;
  private final HttpStatusCode statusCode;
  private final Instant timestamp;
  private final String requestUri;
  private final String httpMethod;

  public ExceptionEvent(Object source, String exceptionType, String message, HttpStatusCode statusCode, String requestUri, String httpMethod) {
    super(source);
    this.exceptionType = exceptionType;
    this.message = message;
    this.statusCode = statusCode;
    this.timestamp = Instant.now();
    this.requestUri = requestUri;
    this.httpMethod = httpMethod;
  }
}
