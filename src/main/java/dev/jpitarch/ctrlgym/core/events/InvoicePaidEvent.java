package dev.jpitarch.ctrlgym.core.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class InvoicePaidEvent extends ApplicationEvent {

  private final String invoiceId;

  private final UUID memberId;

  private final LocalDate nextBillingDate;

  public InvoicePaidEvent(Object source, String invoiceId, UUID memberId, LocalDate nextBillingDate) {
    super(source);
    this.invoiceId = invoiceId;
    this.memberId = memberId;
    this.nextBillingDate = nextBillingDate;
  }
}
