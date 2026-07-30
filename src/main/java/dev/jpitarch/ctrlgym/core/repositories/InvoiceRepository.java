package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.enums.InvoiceStatus;
import dev.jpitarch.ctrlgym.core.models.InvoiceMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.InvoiceJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  private final NamedParameterJdbcTemplate jdbc;

  public Optional<Invoice> getInvoice(String id) {
    return invoiceJpaRepository.findById(id).map(this::mapToDomain);
  }

  public Page<dev.jpitarch.ctrlgym.core.domain.Invoice> findByMemberId(Member.Id memberId, Pageable pageable) {
    return invoiceJpaRepository.findByMemberIdAndGymId(memberId.memberId(), memberId.gymId(), pageable)
      .map(this::mapToDomain);
  }

  private Invoice mapToDomain(InvoiceMO invoiceMO) {
    return Invoice.builder()
      .id(invoiceMO.getId())
      .series(invoiceMO.getSeries())
      .number(invoiceMO.getNumber())
      .issueAt(invoiceMO.getIssueAt())
      .subtotal(invoiceMO.getSubtotal())
      .tax(invoiceMO.getTax())
      .total(invoiceMO.getTotal())
      .build();
  }

  public void create(Invoice invoice, Member.Id memberId) {
    var invoiceMO = createInvoiceMO(invoice, memberId);
    invoiceJpaRepository.save(invoiceMO);
  }

  public void markAsProcessing(String invoiceId) {
    var invoiceMO = invoiceJpaRepository
      .findById(invoiceId)
      .orElseThrow(() -> new RuntimeException("Order reference not found"));

    invoiceMO.setUpdatedAt(OffsetDateTime.now());
    invoiceMO.setStatus(InvoiceStatus.PROCESSING);
    invoiceJpaRepository.save(invoiceMO);
  }

  public void markAsPaid(String invoiceId) {
    var invoiceMO = invoiceJpaRepository
      .findById(invoiceId)
      .orElseThrow(() -> new RuntimeException("Order reference not found"));

    invoiceMO.setUpdatedAt(OffsetDateTime.now());
    invoiceMO.setStatus(InvoiceStatus.PAID);
    invoiceJpaRepository.save(invoiceMO);
  }

  public void markAsFailed(String invoiceId) {
    var invoiceMO = invoiceJpaRepository
      .findById(invoiceId)
      .orElseThrow(() -> new RuntimeException("Order reference not found"));

    invoiceMO.setUpdatedAt(OffsetDateTime.now());
    invoiceMO.setStatus(InvoiceStatus.FAILED);
    invoiceJpaRepository.save(invoiceMO);
  }

  public void saveVerifactuId(String id, UUID verifactuId) {
    var invoiceMO = invoiceJpaRepository
      .findById(id)
      .orElseThrow(() -> new RuntimeException("Order reference not found"));

    log.info("Saving memberId of Verifactu to invoice with memberId {}: {}", id, verifactuId);

    invoiceMO.setUpdatedAt(OffsetDateTime.now());
    invoiceMO.setVerifactuId(verifactuId);
    invoiceJpaRepository.save(invoiceMO);
  }


  private InvoiceMO createInvoiceMO(Invoice invoice, Member.Id memberId) {
    var series = memberId.gymId() + "-" + Year.now();
    var invoiceMO = new InvoiceMO();
    invoiceMO.setId(invoice.getId());
    invoiceMO.setGymId(memberId.gymId());
    invoiceMO.setMemberId(memberId.memberId());
    invoiceMO.setSeries(series);
    invoiceMO.setNumber(this.nextNumber(memberId.gymId(), series).toString());
    invoiceMO.setTotal(invoice.getTotal());
    invoiceMO.setSubtotal(invoice.getSubtotal());
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
