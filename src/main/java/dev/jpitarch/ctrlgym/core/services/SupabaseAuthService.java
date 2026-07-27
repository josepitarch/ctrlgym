package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.dto.SigninRequest;
import dev.jpitarch.ctrlgym.core.dto.SigninResponse;
import dev.jpitarch.ctrlgym.core.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupabaseAuthService {

  @Qualifier("supabaseAuthRestClient")
  private final RestClient supabaseAuthRestClient;

  public void signup(SignupRequest request) {
    supabaseAuthRestClient.post()
      .uri("/signup")
      .body(Map.of(
        "email", request.email(),
        "password", request.password(),
        "data", Map.of(
          "gym_id", request.gymId(),
          "name", request.name(),
          "first_surname", request.firstSurname(),
          "second_surname", request.secondSurname()
        )
      ))
      .retrieve()
      .toBodilessEntity();
  }

  public SigninResponse signin(SigninRequest request) {
    var response = supabaseAuthRestClient.post()
      .uri("/token?grant_type=password")
      .body(Map.of(
        "email", request.email(),
        "password", request.password()
      ))
      .retrieve()
      .body(Map.class);

    return new SigninResponse(
      (String) response.get("access_token"),
      (String) response.get("refresh_token"),
      (Integer) response.get("expires_in"),
      (String) response.get("token_type")
    );
  }
}
