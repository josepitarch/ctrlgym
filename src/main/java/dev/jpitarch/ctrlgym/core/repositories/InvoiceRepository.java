package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.User;
import dev.jpitarch.ctrlgym.core.domain.enums.InvoiceStatus;
import dev.jpitarch.ctrlgym.core.domain.exceptions.InvoiceNotFoundException;
import dev.jpitarch.ctrlgym.core.mappers.InvoiceMapper;
import dev.jpitarch.ctrlgym.core.entities.InvoiceEntity;
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
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InvoiceRepository {

  private final InvoiceJpaRepository invoiceJpaRepository;

  private final NamedParameterJdbcTemplate jdbc;

  private final InvoiceMapper mapper;

  public Optional<Invoice> getInvoice(String id) {
    return invoiceJpaRepository.findById(id).map(mapper::map);
  }

  public Page<Invoice> findByMemberId(UUID memberId, Pageable pageable) {
    return invoiceJpaRepository.findByMemberId(memberId, pageable)
      .map(mapper::map);
  }


  public void create(Invoice invoice, UUID memberId, Integer gymId, Long membershipId) {
    var InvoiceEntity = createInvoiceEntity(invoice, memberId, gymId, membershipId);
    invoiceJpaRepository.save(InvoiceEntity);
  }

  public void markAsProcessing(String invoiceId) {
    var InvoiceEntity = invoiceJpaRepository
      .findById(invoiceId)
      .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

    InvoiceEntity.setDueAt(null);
    InvoiceEntity.setUpdatedAt(OffsetDateTime.now());
    InvoiceEntity.setStatus(InvoiceStatus.PROCESSING);

    invoiceJpaRepository.save(InvoiceEntity);
  }

  public void markAsPaid(String invoiceId) {
    var InvoiceEntity = invoiceJpaRepository
      .findById(invoiceId)
      .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

    InvoiceEntity.setDueAt(null);
    InvoiceEntity.setUpdatedAt(OffsetDateTime.now());
    InvoiceEntity.setStatus(InvoiceStatus.PAID);

    invoiceJpaRepository.save(InvoiceEntity);
  }

  public void markAsFailed(String invoiceId, ZonedDateTime nextAttempt) {
    var InvoiceEntity = invoiceJpaRepository
      .findById(invoiceId)
      .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

    InvoiceEntity.setDueAt(LocalDate.now());
    InvoiceEntity.setUpdatedAt(OffsetDateTime.now());
    InvoiceEntity.setStatus(InvoiceStatus.FAILED);
    InvoiceEntity.setNextAttempt(nextAttempt);

    invoiceJpaRepository.save(InvoiceEntity);
  }

  public void saveVerifactuId(String id, UUID verifactuId) {
    var InvoiceEntity = invoiceJpaRepository
      .findById(id)
      .orElseThrow(() -> new InvoiceNotFoundException(id));

    log.info("Saving memberId of Verifactu to invoice with member with id {}: {}...", id, verifactuId);

    InvoiceEntity.setUpdatedAt(OffsetDateTime.now());
    InvoiceEntity.setVerifactuId(verifactuId);
    invoiceJpaRepository.save(InvoiceEntity);
  }

  public Optional<UUID> getVerifactuId(String invoiceId) {
    return invoiceJpaRepository.getVerifactuId(invoiceId);
  }

  private InvoiceEntity createInvoiceEntity(Invoice invoice, UUID memberId, Integer gymId, Long membershipId) {
    var series = gymId + "-" + Year.now();

    var InvoiceEntity = new InvoiceEntity();
    InvoiceEntity.setId(invoice.getId());
    InvoiceEntity.setGymId(gymId);
    InvoiceEntity.setMemberId(memberId);
    InvoiceEntity.setSeries(series);
    InvoiceEntity.setNumber(this.nextNumber(gymId, series).toString());
    InvoiceEntity.setTotal(invoice.getTotal());
    InvoiceEntity.setSubtotal(invoice.getSubtotal());
    InvoiceEntity.setCurrency(invoice.getCurrency());
    InvoiceEntity.setIssueAt(LocalDate.now());
    InvoiceEntity.setStatus(InvoiceStatus.OPEN);
    InvoiceEntity.setTax(BigDecimal.valueOf(Invoice.TAX));
    InvoiceEntity.setCreatedAt(OffsetDateTime.now());
    InvoiceEntity.setUpdatedAt(OffsetDateTime.now());
    InvoiceEntity.setMembershipId(membershipId);

    return InvoiceEntity;
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
