package dev.jpitarch.ctrlgym.verifactu.services;

import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.verifactu.dtos.ValidateNifRequest;
import dev.jpitarch.ctrlgym.verifactu.dtos.ValidateNifResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class NifValidationService {

  private final RestClient restClient;

  private final GymsRepository gymsRepository;

  public NifValidationService(RestClient.Builder builder, GymsRepository gymsRepository) {
    this.gymsRepository = gymsRepository;
    this.restClient = builder
      .baseUrl("https://api.verifacti.com")
      .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .build();
  }

  public @Nullable ValidateNifResponse validateNif(Integer gymId, String nif) {
    return validateNif(gymId, nif, null);
  }

  public @Nullable ValidateNifResponse validateNif(Integer gymId, String nif, @Nullable String name) {
    var apiKey = gymsRepository.getVerifactuApiKey(gymId);

    var request = new ValidateNifRequest(nif, name);

    log.info("Validating NIF {} for gym {}...", nif, gymId);

    return restClient.post()
      .uri("/nifs/validar")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
      .body(request)
      .retrieve()
      .body(ValidateNifResponse.class);
  }
}
