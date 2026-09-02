package dev.jpitarch.ctrlgym.core.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class OrderCreatedEvent extends ApplicationEvent {

  private final Integer orderId;

  private final UUID memberId;

  private final Integer gymBranchId;

  private final Integer gymId;

  private final Integer itemCount;

  public OrderCreatedEvent(Object source, Integer orderId, UUID memberId, Integer gymBranchId, Integer gymId, Integer itemCount) {
    super(source);
    this.orderId = orderId;
    this.memberId = memberId;
    this.gymBranchId = gymBranchId;
    this.gymId = gymId;
    this.itemCount = itemCount;
  }
}
