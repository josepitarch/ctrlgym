package dev.jpitarch.ctrlgym.verifactu.service;

import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.verifactu.dto.CreateInvoiceRequest;
import dev.jpitarch.ctrlgym.verifactu.dto.CreateInvoiceResponse;
import dev.jpitarch.ctrlgym.verifactu.dto.StatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifactuService {

  private final WebClient webClient;

  private final GymsRepository gymsRepository;

  public CreateInvoiceResponse createInvoice(Integer gymId, CreateInvoiceRequest request, String idempotencyKey) {
    var apiKey = gymsRepository.getApiKey(gymId);
    return webClient.post()
      .uri("/create")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
      //.header("Idempotency-Key", idempotencyKey)
      .bodyValue(request)
      .retrieve()
      .bodyToMono(CreateInvoiceResponse.class)
      .block();
  }

  public StatusResponse getStatus(Integer gymId, UUID uuid) {
    var apiKey = gymsRepository.getApiKey(gymId);
    return webClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/status")
        .queryParam("uuid", uuid)
        .build())
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
      .retrieve()
      .bodyToMono(StatusResponse.class)
      .block();
  }
}
