package dev.jpitarch.ctrlgym.core.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

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
