package dev.jpitarch.ctrlgym.payments.repositories;

import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import dev.jpitarch.ctrlgym.core.domain.enums.InvoiceStatus;
import dev.jpitarch.ctrlgym.core.domain.enums.PaymentStatus;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.payments.models.InvoiceMO;
import dev.jpitarch.ctrlgym.payments.models.PaymentMO;
import dev.jpitarch.ctrlgym.payments.repositories.jpa.InvoiceJpaRepository;
import dev.jpitarch.ctrlgym.payments.repositories.jpa.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Repository
@RequiredArgsConstructor
public class InvoiceRepository {

  private final InvoiceJpaRepository invoiceJpaRepository;

  private final PaymentJpaRepository paymentJpaRepository;

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  public void create(Invoice invoice) {
    var invoiceMO = new InvoiceMO();
    invoiceMO.setId(invoice.getId());
    invoiceMO.setGymId(1);
    invoiceMO.setMemberId(membersRepository.getId(invoice.getCustomer()));
    invoiceMO.setInvoiceNumber(invoice.getNumber());
    invoiceMO.setTotal(BigDecimal.valueOf(invoice.getTotal()));
    invoiceMO.setSubtotal(BigDecimal.valueOf(invoice.getSubtotal()));
    invoiceMO.setCurrency(invoice.getCurrency());
    invoiceMO.setIssueAt(LocalDate.now());
    invoiceMO.setDueAt(LocalDate.now());
    invoiceMO.setStatus(InvoiceStatus.OPEN);
    invoiceMO.setTax(BigDecimal.valueOf(0));
    invoiceMO.setCreatedAt(OffsetDateTime.now());
    invoiceMO.setUpdatedAt(OffsetDateTime.now());

    invoiceJpaRepository.save(invoiceMO);
  }

  public void markAsProcessing(PaymentIntent paymentIntent) {
    var paymentMO = new PaymentMO();
    paymentMO.setStripePaymentIntentId(paymentIntent.getId());
    paymentMO.setId(paymentIntent.getPaymentDetails().getOrderReference());
    paymentMO.setStatus(PaymentStatus.PROCESSING);
    paymentMO.setAmount(BigDecimal.valueOf(paymentIntent.getAmount()));
    paymentMO.setCreatedAt(OffsetDateTime.now());

    paymentJpaRepository.save(paymentMO);
  }

  public void markAsPaid(Invoice invoice) {

  }

  public void markAsFailed(Invoice invoice) {
  }

}
