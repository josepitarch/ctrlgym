package dev.jpitarch.ctrlgym.payments.repositories;

import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import dev.jpitarch.ctrlgym.core.domain.enums.InvoiceStatus;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.payments.models.InvoiceMO;
import dev.jpitarch.ctrlgym.payments.repositories.jpa.InvoiceJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InvoiceRepository {

  private final InvoiceJpaRepository invoiceJpaRepository;

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  public void create(Invoice invoice) {
    var invoiceMO = createInvoiceMO(invoice, InvoiceStatus.OPEN);
    invoiceJpaRepository.save(invoiceMO);
  }

  public void markAsProcessing(PaymentIntent paymentIntent) {
    invoiceJpaRepository.findById(paymentIntent.getPaymentDetails().getOrderReference())
      .ifPresentOrElse(invoiceMO -> {
        invoiceMO.setStatus(InvoiceStatus.PROCESSING);
        invoiceJpaRepository.save(invoiceMO);
      }, () -> {
        var invoiceMO = createInvoiceMO(invoice, InvoiceStatus.OPEN);
        invoiceJpaRepository.save(invoiceMO);
      });
  }

  public void markAsPaid(Invoice invoice) {
    invoiceJpaRepository.findById(invoice.getId())
      .ifPresentOrElse(invoiceMO -> {
        invoiceMO.setStatus(InvoiceStatus.PAID);
        invoiceJpaRepository.save(invoiceMO);
      }, () -> {
        log.warn("Invoice with id {} is not created when payment succeeded", invoice.getId());
        var invoiceMO = createInvoiceMO(invoice, InvoiceStatus.PAID);
        invoiceJpaRepository.save(invoiceMO);
      });

  }

  public void markAsFailed(Invoice invoice) {
    invoiceJpaRepository.findById(invoice.getId())
      .ifPresentOrElse(invoiceMO -> {
        invoiceMO.setStatus(InvoiceStatus.FAILED);
        invoiceJpaRepository.save(invoiceMO);
      }, () -> {
        log.warn("Invoice with id {} is not created when payment failed", invoice.getId());
        var invoiceMO = createInvoiceMO(invoice, InvoiceStatus.FAILED);
        invoiceJpaRepository.save(invoiceMO);
      });
  }

  private InvoiceMO createInvoiceMO(Invoice invoice, InvoiceStatus status) {
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
    invoiceMO.setStatus(status);
    invoiceMO.setTax(BigDecimal.valueOf(0));
    invoiceMO.setCreatedAt(OffsetDateTime.now());
    invoiceMO.setUpdatedAt(OffsetDateTime.now());

    return invoiceMO;
  }

}
