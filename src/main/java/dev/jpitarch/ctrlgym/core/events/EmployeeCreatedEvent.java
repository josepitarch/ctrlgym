package dev.jpitarch.ctrlgym.core.events;

import dev.jpitarch.ctrlgym.core.domain.enums.Role;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class EmployeeCreatedEvent extends ApplicationEvent {

  private final UUID employeeId;

  private final Integer gymId;

  private final String email;

  public EmployeeCreatedEvent(Object source, UUID employeeId, Integer gymId, String email) {
    super(source);
    this.employeeId = employeeId;
    this.gymId = gymId;
    this.email = email;
  }
}
