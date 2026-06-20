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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InvoiceRepository {

  private final InvoiceJpaRepository invoiceJpaRepository;

  private final GymsRepository gymsRepository;

  private final MembersRepository membersRepository;

  private final NamedParameterJdbcTemplate jdbc;

  public Optional<InvoiceMO> getInvoice(String id) {
    return invoiceJpaRepository.findById(id);
  }

  public void create(Invoice invoice, String stripeAccountId) {
    var gymId = gymsRepository.getId(stripeAccountId);
    var invoiceMO = createInvoiceMO(invoice, gymId);
    invoiceJpaRepository.save(invoiceMO);
  }

  public void markAsProcessing(PaymentIntent paymentIntent) {
    var invoiceMO = invoiceJpaRepository
      .findById(paymentIntent.getPaymentDetails().getOrderReference())
      .orElseThrow(() -> new RuntimeException("Order reference not found"));

    invoiceMO.setUpdatedAt(OffsetDateTime.now());
    invoiceMO.setStatus(InvoiceStatus.PROCESSING);
    invoiceJpaRepository.save(invoiceMO);
  }

  public void markAsPaid(Invoice invoice) {
    var invoiceMO = invoiceJpaRepository
      .findById(invoice.getId())
      .orElseThrow(() -> new RuntimeException("Order reference not found"));

    invoiceMO.setUpdatedAt(OffsetDateTime.now());
    invoiceMO.setStatus(InvoiceStatus.PAID);
    invoiceJpaRepository.save(invoiceMO);
  }

  public void markAsFailed(Invoice invoice) {
    var invoiceMO = invoiceJpaRepository
      .findById(invoice.getId())
      .orElseThrow(() -> new RuntimeException("Order reference not found"));

    invoiceMO.setUpdatedAt(OffsetDateTime.now());
    invoiceMO.setStatus(InvoiceStatus.FAILED);
    invoiceJpaRepository.save(invoiceMO);
  }

  public void saveVerifactuId(String id, UUID verifactuId) {
    var invoiceMO = invoiceJpaRepository
      .findById(id)
      .orElseThrow(() -> new RuntimeException("Order reference not found"));

    log.info("Saving id of Verifactu to invoice with id {}: {}", id, verifactuId);

    invoiceMO.setUpdatedAt(OffsetDateTime.now());
    invoiceMO.setVerifactuId(verifactuId);
    invoiceJpaRepository.save(invoiceMO);
  }


  private InvoiceMO createInvoiceMO(Invoice invoice, Integer gymId) {
    var series = gymId + "-" + Year.now();
    var invoiceMO = new InvoiceMO();
    invoiceMO.setId(invoice.getId());
    invoiceMO.setGymId(gymId);
    invoiceMO.setMemberId(membersRepository.getId(invoice.getCustomer()).id());
    invoiceMO.setSeries(series);
    invoiceMO.setNumber(this.nextNumber(gymId, series).toString());
    invoiceMO.setStripeInvoiceNumber(invoice.getNumber());
    invoiceMO.setTotal(BigDecimal.valueOf(invoice.getTotal()));
    invoiceMO.setSubtotal(BigDecimal.valueOf(invoice.getSubtotal()));
    invoiceMO.setCurrency(invoice.getCurrency());
    invoiceMO.setIssueAt(LocalDate.now());
    invoiceMO.setDueAt(LocalDate.now());
    invoiceMO.setStatus(InvoiceStatus.OPEN);
    invoiceMO.setTax(BigDecimal.valueOf(21));
    invoiceMO.setCreatedAt(OffsetDateTime.now());
    invoiceMO.setUpdatedAt(OffsetDateTime.now());

    return invoiceMO;
  }

  private Integer nextNumber(Integer gymId, String series) {
    String sql = """
      INSERT INTO invoice_counter (gym_id, series, last_number)
      VALUES (:gymId, :series, 1)
      ON CONFLICT (gym_id, series)
      DO UPDATE SET last_number = invoice_counter.last_number + 1
      RETURNING last_number
      """;

    var params = Map.of(
      "gymId", gymId,
      "series", series
    );

    return jdbc.queryForObject(sql, params, Integer.class);
  }

}
