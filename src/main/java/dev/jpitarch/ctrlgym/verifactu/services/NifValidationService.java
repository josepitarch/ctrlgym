package dev.jpitarch.ctrlgym.verifactu.services;

import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
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

  public boolean validateNif(Integer gymId, String nif, @Nullable String name) {
    var apiKey = gymsRepository.getVerifactuApiKey(gymId);

    log.info("Validating NIF {} and name {} for gym {}.", nif, name, gymId);

    return true;

    /*var response = restClient.post()
      .uri("/nifs/validar")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
      .body(new ValidateNifRequest(nif, name))
      .retrieve()
      .body(ValidateNifResponse.class);

    return response != null && response.result() == NifValidationResult.IDENTIFICADO;
    */
  }
}
