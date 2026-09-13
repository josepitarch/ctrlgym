package dev.jpitarch.ctrlgym.core.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class GuardianAuthorizationRequiredEvent extends ApplicationEvent {

  private final UUID memberId;

  private final Integer gymId;

  private final String guardianEmail;

  public GuardianAuthorizationRequiredEvent(Object source, UUID memberId, Integer gymId, String guardianEmail) {
    super(source);
    this.memberId = memberId;
    this.gymId = gymId;
    this.guardianEmail = guardianEmail;
  }
}
