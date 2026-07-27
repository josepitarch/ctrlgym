package dev.jpitarch.ctrlgym.core.usecases;

import dev.jpitarch.ctrlgym.core.dto.SigninRequest;
import dev.jpitarch.ctrlgym.core.dto.AuthResponse;
import dev.jpitarch.ctrlgym.core.dto.SignupRequest;
import dev.jpitarch.ctrlgym.core.models.UserMO;
import dev.jpitarch.ctrlgym.core.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

  private final RestClient supabaseAuthRestClient;

  private final UsersRepository usersRepository;

  public AuthService(@Qualifier("supabaseAuthRestClient") RestClient supabaseAuthRestClient, UsersRepository usersRepository) {
    this.supabaseAuthRestClient = supabaseAuthRestClient;
    this.usersRepository = usersRepository;
  }

  public AuthResponse signup(SignupRequest request) {
    var existingUserId = usersRepository.findIdByEmail(request.email());

    if (existingUserId.isPresent()) {
      insertUserRow(existingUserId.get(), request);
      return signin(new SigninRequest(request.email(), request.password()));
    } else {
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

  private void insertUserRow(UUID userId, SignupRequest request) {
    var user = new UserMO();
    user.setId(userId);
    user.setGymId(request.gymId());
    user.setEmail(request.email());
    user.setName(request.name());
    user.setFirstSurname(request.firstSurname());
    user.setSecondSurname(request.secondSurname());
    usersRepository.save(user);
  }
}
