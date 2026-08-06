package dev.jpitarch.ctrlgym.verifactu.service;

import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.events.InvoicePaidEvent;
import dev.jpitarch.ctrlgym.notifications.TelegramNotificationService;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.services.InvoiceService;
import dev.jpitarch.ctrlgym.verifactu.dto.CreateInvoiceRequest;
import dev.jpitarch.ctrlgym.verifactu.dto.CreateInvoiceResponse;
import dev.jpitarch.ctrlgym.verifactu.dto.StatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
public class VerifactuService {

  private final RestClient restClient;

  private final GymsRepository gymsRepository;

  private final InvoiceRepository invoiceRepository;

  private final InvoiceService invoiceService;

  private final TelegramNotificationService telegramNotificationService;

  private final RetryTemplate retryTemplate = new RetryTemplate(
    RetryPolicy.builder()
      .includes(HttpServerErrorException.class)
      .maxRetries(2)
      .delay(Duration.ofMillis(500))
      .multiplier(2)
      .build()
  );

  public VerifactuService(RestClient.Builder builder, GymsRepository gymsRepository, InvoiceRepository invoiceRepository, InvoiceService invoiceService, TelegramNotificationService telegramNotificationService) {
    this.gymsRepository = gymsRepository;
    this.invoiceRepository = invoiceRepository;
    this.invoiceService = invoiceService;
    this.telegramNotificationService = telegramNotificationService;
    this.restClient = builder
      .baseUrl("https://api.verifacti.com/verifactu")
      .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .build();
  }

  @Retryable(includes = HttpServerErrorException.class)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void createInvoice(InvoicePaidEvent event) {
    var apiKey = gymsRepository.getVerifactuApiKey(event.getMemberId().gymId());
    var invoice = invoiceService.getInvoiceWithMemberData(event.getInvoiceId());

    var body = CreateInvoiceRequest.builder()
            .serie(invoice.getSeries())
            .numero(invoice.getNumber())
            .expeditionDate(invoice.getIssueAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
            .invoiceType("F1")
            .name(invoice.getFullName())
            .nif(invoice.getNif())
            .description("Factura normal")
            .lines(Collections.singletonList(CreateInvoiceRequest.Line.builder()
              .taxableBase(invoice.getSubtotal().toString())
              .taxRate(Invoice.TAX.toString())
              .repercussedQuota(this.getTaxes(invoice).toString())
              .build()
            ))
            .totalAmount(invoice.getTotal().toString())
            .build();

    log.info("Calling to Verifactu for invoice with memberId {}...", invoice.getId());

    try {

      retryTemplate.invoke(() -> {
        var response = restClient.post()
          .uri("/create")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
          .body(body)
          .retrieve()
          .body(CreateInvoiceResponse.class);

        assert response != null;
        invoiceRepository.saveVerifactuId(invoice.getId(), response.uuid());
      });


    } catch(HttpServerErrorException e) {
      log.error("Attempts has been exceeded. Reason was: {}", e.getMessage(), e);
      telegramNotificationService.send("Verifactu seems KO. All attempts has been exceeded. Check logs!");

    } catch(Exception e) {
      log.error("Unexpected error has occurred: {}", e.getMessage(), e);
      telegramNotificationService.send("Unexpected error has occurred calling Verifactu API. Check logs!");
    }


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
