package dev.jpitarch.ctrlgym.core.usecases;

import dev.jpitarch.ctrlgym.core.dto.SigninRequest;
import dev.jpitarch.ctrlgym.core.dto.SigninResponse;
import dev.jpitarch.ctrlgym.core.dto.SignupRequest;
import dev.jpitarch.ctrlgym.core.models.UserMO;
import dev.jpitarch.ctrlgym.core.repositories.UsersRepository;
import dev.jpitarch.ctrlgym.core.services.SupabaseAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthUseCase {

  private final SupabaseAuthService supabaseAuthService;
  private final UsersRepository usersRepository;

  public void signup(SignupRequest request) {
    var existingUserId = usersRepository.findIdByEmail(request.email());

    if (existingUserId.isPresent()) {
      insertUserRow(existingUserId.get(), request);
    } else {
      supabaseAuthService.signup(request);
    }
  }

  public SigninResponse signin(SigninRequest request) {
    return supabaseAuthService.signin(request);
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
