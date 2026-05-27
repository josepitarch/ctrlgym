package dev.jpitarch.ctrlgym.verifactu.service;

import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.verifactu.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class VerifactuService {

  private final WebClient webClient;

  private final GymsRepository gymsRepository;

  public CreateInvoiceResponse createFactura(Integer gymId, CreateInvoiceRequest request) {
    var apiKey = gymsRepository.getApiKey(gymId);
    return webClient.post()
      .uri("/create")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
      .bodyValue(request)
      .retrieve()
      .bodyToMono(CreateInvoiceResponse.class)
      .block();
  }

  public CreateInvoiceResponse createFacturaWithIdempotency(CreateInvoiceRequest request, String idempotencyKey) {
    return webClient.post()
      .uri("/create")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + "")
      .header("Idempotency-Key", idempotencyKey)
      .bodyValue(request)
      .retrieve()
      .bodyToMono(CreateInvoiceResponse.class)
      .block();
  }

  public StatusResponse getStatus(String uuid) {
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
