package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.exceptions.AuthException;
import dev.jpitarch.ctrlgym.core.dto.AuthResponse;
import dev.jpitarch.ctrlgym.core.dto.SigninRequest;
import dev.jpitarch.ctrlgym.core.dto.SignupRequest;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

  private final RestClient supabaseAuthRestClient;

  private final MembersRepository membersRepository;

  public AuthService(RestClient.Builder builder,
                     @Value("${supabase.url}") String supabaseUrl,
                     @Value("${supabase.service-role-key}") String serviceRoleKey,
                     MembersRepository membersRepository) {
    this.supabaseAuthRestClient = builder
      .baseUrl(supabaseUrl + "/auth/v1")
      .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .defaultHeader("apikey", serviceRoleKey)
      .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
      .build();
    this.membersRepository = membersRepository;
  }

  public AuthResponse signup(SignupRequest request) {
    if (membersRepository.exists(request.gymId(), request.email())) {
      if (membersRepository.isInMigration(request.gymId(), request.email())) {
        log.info("User with email {} of gym with id {} is in migration yet. Sending a new invitation...", request.email(), request.gymId());
        supabaseAuthRestClient.post()
          .uri("/invite")
          .body(Map.of("email", request.email()))
          .retrieve()
          .toBodilessEntity();

        throw new AuthException(AuthException.Signup.IS_IN_MIGRATION, request.gymId(), request.email());
      }

      throw new AuthException(AuthException.Signup.ALREADY_EXISTS, request.gymId(), request.email());
    }

    if (membersRepository.existsAnotherGym(request.gymId(), request.email())) {
      //TODO: crear para este nuevo gimnasio solicitado solo si es miembro
      throw new AuthException(AuthException.Signup.ANOTHER_GYM, request.gymId(), request.email());
    }

    log.info("Registering a new user with email {} associated to gym with id {}...", request.email(), request.gymId());
    return supabaseAuthRestClient.post()
      .uri("/signup")
      .body(Map.of(
        "email", request.email(),
        "password", request.password(),
        "data", new HashMap<String, Object>() {{
          put("gym_id", request.gymId());
          put("name", request.name());
          put("first_surname", request.firstSurname());
          put("second_surname", request.secondSurname());
        }}
      ))
      .retrieve()
      .body(AuthResponse.class);
  }


  public AuthResponse login(SigninRequest request) {
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
