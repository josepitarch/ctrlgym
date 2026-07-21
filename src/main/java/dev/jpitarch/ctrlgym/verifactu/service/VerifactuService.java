package dev.jpitarch.ctrlgym.verifactu.service;

import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.payments.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.verifactu.dto.CreateInvoiceRequest;
import dev.jpitarch.ctrlgym.verifactu.dto.CreateInvoiceResponse;
import dev.jpitarch.ctrlgym.verifactu.dto.StatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifactuService {

  private final RestClient restClient;

  private final GymsRepository gymsRepository;

  private final InvoiceRepository invoiceRepository;

  @Retryable(includes = HttpServerErrorException.class)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void createInvoice(Invoice invoice) {
    var apiKey = gymsRepository.getVerifactuApiKey(1);
    var body = CreateInvoiceRequest.builder()
      .serie(invoice.getSeries())
      .numero(invoice.getNumber())
      .expeditionDate(invoice.getIssueAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
      .invoiceType("F1")
      .name(invoice.getFullName())
      .nif(invoice.getNif())
      .description("Probando...")
      .lines(Collections.singletonList(CreateInvoiceRequest.Line.builder()
        .taxableBase(invoice.getSubtotal().toString())
        .taxRate(Invoice.TAX.toString())
        .repercussedQuota(this.getTaxes(invoice).toString())
        .build()
      ))
      .totalAmount(invoice.getTotal().toString())
      .build();

    log.info("Calling to Verifacti for invoice with memberId {}...", invoice.getId());

    var response = restClient.post()
      .uri("/create")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
      .body(body)
      .retrieve()
      .body(CreateInvoiceResponse.class);

    assert response != null;
    invoiceRepository.saveVerifactuId(invoice.getId(), response.uuid());
  }

  public StatusResponse getStatus(Integer gymId, UUID uuid) {
    var apiKey = gymsRepository.getVerifactuApiKey(gymId);
    return restClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/status")
        .queryParam("uuid", uuid)
        .build())
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
      .retrieve()
      .body(StatusResponse.class);
  }

  private BigDecimal getTaxes(Invoice invoice) {
    return invoice.getTotal().subtract(invoice.getSubtotal());
  }

}
