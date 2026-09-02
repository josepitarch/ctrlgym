package dev.jpitarch.ctrlgym.verifactu.services;

import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.events.InvoicePaidEvent;
import dev.jpitarch.ctrlgym.core.events.OrderCreatedEvent;
import dev.jpitarch.ctrlgym.notifications.services.TelegramNotificationService;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.OrderRepository;
import dev.jpitarch.ctrlgym.core.services.InvoiceService;
import dev.jpitarch.ctrlgym.verifactu.dtos.CreateInvoiceRequest;
import dev.jpitarch.ctrlgym.verifactu.dtos.CreateInvoiceResponse;
import dev.jpitarch.ctrlgym.verifactu.dtos.StatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
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
import java.util.function.Consumer;

@Slf4j
@Service
public class VerifactuService {

  private final RestClient restClient;

  private final GymsRepository gymsRepository;

  private final InvoiceRepository invoiceRepository;

  private final OrderRepository orderRepository;

  private final InvoiceService invoiceService;

  private final MembersRepository membersRepository;

  private final TelegramNotificationService telegramNotificationService;

  private final RetryTemplate retryTemplate = new RetryTemplate(
    RetryPolicy.builder()
      .includes(HttpServerErrorException.class)
      .maxRetries(2)
      .delay(Duration.ofMillis(500))
      .multiplier(2)
      .build()
  );

  private static final String NORMAL_INVOICE = "F1";

  private static final String SIMPLIFIED_INVOICE = "F2";

  public VerifactuService(RestClient.Builder builder, GymsRepository gymsRepository, InvoiceRepository invoiceRepository,
                          OrderRepository orderRepository, InvoiceService invoiceService, MembersRepository membersRepository,
                          TelegramNotificationService telegramNotificationService) {
    this.gymsRepository = gymsRepository;
    this.invoiceRepository = invoiceRepository;
    this.orderRepository = orderRepository;
    this.invoiceService = invoiceService;
    this.membersRepository = membersRepository;
    this.telegramNotificationService = telegramNotificationService;
    this.restClient = builder
      .baseUrl("https://api.verifacti.com/verifactu")
      .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .build();
  }


  public @Nullable StatusResponse getStatus(Integer gymId, String invoiceId) {
    var apiKey = gymsRepository.getVerifactuApiKey(gymId);
    var verifactuId = invoiceRepository.getVerifactuId(invoiceId);

    if (verifactuId.isEmpty()) return null;

    return restClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/status")
        .queryParam("uuid", verifactuId)
        .build())
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
      .retrieve()
      .body(StatusResponse.class);
  }

  @Retryable(includes = HttpServerErrorException.class)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onInvoicePaid(InvoicePaidEvent event) {
    processInvoice(event.getInvoiceId());
  }

  public void processInvoice(String invoiceId) {
    var invoice = invoiceService.getInvoiceWithMemberData(invoiceId);
    var gymId = membersRepository.getGymIdByMemberId(invoice.getMemberId());
    var apiKey = gymsRepository.getVerifactuApiKey(gymId);

    var body = CreateInvoiceRequest.builder()
      .serie(invoice.getSeries())
      .numero(invoice.getNumber())
      .expeditionDate(invoice.getIssueAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
      .invoiceType(NORMAL_INVOICE)
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

    log.info("Calling to Verifactu for invoice with id {}...", invoice.getId());

    doRequest(apiKey, body, response -> invoiceRepository.saveVerifactuId(invoice.getId(), response.uuid()));
  }


  @Retryable(includes = HttpServerErrorException.class)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void listenOrderEvents(OrderCreatedEvent event) {
    var order = orderRepository.findById(event.getOrderId())
      .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

    var apiKey = gymsRepository.getVerifactuApiKey(event.getGymId());

    var subtotal = order.getItems().stream()
      .map(item -> item.getProductPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    var tax = subtotal.multiply(BigDecimal.valueOf(Invoice.TAX)).divide(BigDecimal.valueOf(100));
    var total = subtotal.add(tax);

    var body = CreateInvoiceRequest.builder()
      .serie(order.getSeries())
      .numero(order.getNumber())
      .expeditionDate(order.getCreatedAt().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
      .invoiceType(SIMPLIFIED_INVOICE)
      .description("Factura simplificada")
      .lines(Collections.singletonList(CreateInvoiceRequest.Line.builder()
        .taxableBase(subtotal.toString())
        .taxRate(String.valueOf(Invoice.TAX))
        .repercussedQuota(tax.toString())
        .build()
      ))
      .totalAmount(total.toString())
      .build();

    log.info("Calling to Verifactu for order with id {}...", order.getId());

    doRequest(apiKey, body, response -> orderRepository.saveVerifactuId(order.getId(), response.uuid()));
  }

  private void doRequest(String apiKey, CreateInvoiceRequest body, Consumer<CreateInvoiceResponse> onSave) {
    try {
      retryTemplate.invoke(() -> {
        var response = restClient.post()
          .uri("/create")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
          .body(body)
          .retrieve()
          .body(CreateInvoiceResponse.class);

        assert response != null;
        onSave.accept(response);
      });

    } catch (HttpServerErrorException e) {
      log.error("Attempts has been exceeded. Reason was: {}", e.getMessage(), e);
      telegramNotificationService.send("Verifactu seems KO. All attempts has been exceeded. Check logs!");

    } catch (Exception e) {
      log.error("Unexpected error has occurred: {}", e.getMessage(), e);
      telegramNotificationService.send("Unexpected error has occurred calling Verifactu API. Check logs!");
    }
  }

  private BigDecimal getTaxes(Invoice invoice) {
    return invoice.getTotal().subtract(invoice.getSubtotal());
  }

}
