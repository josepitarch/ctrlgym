package dev.jpitarch.ctrlgym.verifactu.service;

import dev.jpitarch.ctrlgym.core.domain.Invoice;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.events.InvoicePaidEvent;
import dev.jpitarch.ctrlgym.core.notifications.TelegramNotificationService;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.services.InvoiceService;
import dev.jpitarch.ctrlgym.verifactu.dto.CreateInvoiceRequest;
import dev.jpitarch.ctrlgym.verifactu.dto.CreateInvoiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifactuServiceTest {


  VerifactuService verifactuService;

  @Mock
  RestClient.Builder restClientBuilder;

  @Mock
  RestClient verifactuRestClient;

  @Mock
  GymsRepository gymsRepository;

  @Mock
  InvoiceRepository invoiceRepository;

  @Mock
  InvoiceService invoiceService;

  @Mock
  TelegramNotificationService telegramNotificationService;

  @Mock
  RestClient.RequestBodyUriSpec requestBodyUriSpec;

  @Mock
  RestClient.RequestBodySpec requestBodySpec;

  @Mock
  RestClient.ResponseSpec responseSpec;

  private final UUID verifactuUuid = UUID.randomUUID();
  private final CreateInvoiceResponse createInvoiceResponse = new CreateInvoiceResponse(verifactuUuid);

  private final Invoice invoice = Invoice.builder()
    .id("inv-001")
    .name("Juan")
    .firstSurname("García")
    .secondSurname("López")
    .nif("12345678A")
    .series("A")
    .number("001")
    .issueAt(LocalDate.of(2025, 3, 15))
    .subtotal(new BigDecimal("100.00"))
    .tax(new BigDecimal("21.00"))
    .total(new BigDecimal("121.00"))
    .currency("EUR")
    .build();

  private final Member.Id memberId = new Member.Id(UUID.randomUUID(), 1);

  @BeforeEach
  void setUp() {
    lenient().when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
    lenient().when(restClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(restClientBuilder);
    lenient().when(restClientBuilder.build()).thenReturn(verifactuRestClient);
    lenient().when(verifactuRestClient.post()).thenReturn(requestBodyUriSpec);
    lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    lenient().when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
    lenient().when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
    lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);

    verifactuService = new VerifactuService(restClientBuilder, gymsRepository, invoiceRepository, invoiceService, telegramNotificationService);
  }

  @Test
  @DisplayName("createInvoice sends all body fields correctly and saves verifactu id")
  void createInvoice_sendsCorrectBodyAndSavesId() {
    var event = new InvoicePaidEvent(this, "inv-001", memberId, LocalDate.of(2025, 4, 1));

    when(gymsRepository.getVerifactuApiKey(1)).thenReturn("test-api-key");
    when(invoiceService.getInvoiceWithMemberData("inv-001")).thenReturn(invoice);
    when(responseSpec.body(CreateInvoiceResponse.class)).thenReturn(createInvoiceResponse);

    verifactuService.createInvoice(event);

    verify(requestBodyUriSpec).uri("/create");
    verify(requestBodySpec).header("Authorization", "Bearer test-api-key");

    ArgumentCaptor<CreateInvoiceRequest> bodyCaptor = ArgumentCaptor.forClass(CreateInvoiceRequest.class);
    verify(requestBodySpec).body(bodyCaptor.capture());

    CreateInvoiceRequest body = bodyCaptor.getValue();
    assertThat(body.getSerie()).isEqualTo("A");
    assertThat(body.getNumero()).isEqualTo("001");
    assertThat(body.getExpeditionDate()).isEqualTo("15-03-2025");
    assertThat(body.getInvoiceType()).isEqualTo("F1");
    assertThat(body.getName()).isEqualTo("Juan García López");
    assertThat(body.getNif()).isEqualTo("12345678A");
    assertThat(body.getDescription()).isEqualTo("Probando...");
    assertThat(body.getTotalAmount()).isEqualTo("121.00");

    assertThat(body.getLines()).hasSize(1);
    var line = body.getLines().getFirst();
    assertThat(line.getTaxableBase()).isEqualTo("100.00");
    assertThat(line.getTaxRate()).isEqualTo("21");
    assertThat(line.getRepercussedQuota()).isEqualTo("21.00");

    verify(invoiceRepository).saveVerifactuId("inv-001", verifactuUuid);
  }
}
