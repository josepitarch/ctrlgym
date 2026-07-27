package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.dto.SigninRequest;
import dev.jpitarch.ctrlgym.core.dto.AuthResponse;
import dev.jpitarch.ctrlgym.core.dto.SignupRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class SupabaseAuthService {

  private final RestClient supabaseAuthRestClient;

  public SupabaseAuthService(@Qualifier("supabaseAuthRestClient") RestClient supabaseAuthRestClient) {
    this.supabaseAuthRestClient = supabaseAuthRestClient;
  }

  public AuthResponse signup(SignupRequest request) {
    return supabaseAuthRestClient.post()
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
      .body(AuthResponse.class);
  }

  public AuthResponse signin(SigninRequest request) {
    return supabaseAuthRestClient.post()
      .uri("/token?grant_type=password")
      .body(Map.of(
        "email", request.email(),
        "password", request.password()
      ))
      .retrieve()
      .body(AuthResponse.class);
  }

}
