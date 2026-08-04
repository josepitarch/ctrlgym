package dev.jpitarch.ctrlgym.core.events;

import dev.jpitarch.ctrlgym.core.domain.Member;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

@Getter
public class InvoicePaidEvent extends ApplicationEvent {

  private final String invoiceId;

  private final Member.Id memberId;

  private final LocalDate nextBillingDate;

  public InvoicePaidEvent(Object source, String invoiceId, Member.Id memberId, LocalDate nextBillingDate) {
    super(source);
    this.invoiceId = invoiceId;
    this.memberId = memberId;
    this.nextBillingDate = nextBillingDate;
  }
}
