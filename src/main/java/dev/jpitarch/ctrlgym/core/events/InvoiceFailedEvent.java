package dev.jpitarch.ctrlgym.core.events;

import dev.jpitarch.ctrlgym.core.domain.Member;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

@Getter
public class InvoiceFailedEvent extends ApplicationEvent {

  private final String invoiceId;

  private final String subscriptionId;

  public InvoiceFailedEvent(Object source, String invoiceId, String subscriptionId) {
    super(source);
    this.invoiceId = invoiceId;

    this.subscriptionId = subscriptionId;
  }

}
