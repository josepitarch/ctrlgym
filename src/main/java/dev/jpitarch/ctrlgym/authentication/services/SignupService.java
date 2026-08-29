package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.dtos.AuthResponse;
import dev.jpitarch.ctrlgym.authentication.dtos.SignupRequest;
import dev.jpitarch.ctrlgym.authentication.repositories.UserRepository;
import dev.jpitarch.ctrlgym.core.domain.enums.Role;
import dev.jpitarch.ctrlgym.core.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtService jwtService;

  private final RefreshTokenService refreshTokenService;

  public AuthResponse signup(SignupRequest request) {
    String hashedPassword = passwordEncoder.encode(request.password());
    UserEntity created = userRepository.create(
      request.email(),
      hashedPassword,
      request.gymId(),
      request.name(),
      request.firstSurname(),
      request.secondSurname()
    );

    String accessToken = jwtService.generateAccessToken(created.getId(), request.gymId(), Role.MEMBER);
    String rawRefreshToken = refreshTokenService.generateRawRefreshToken(created.getId(), request.gymId());

    return new AuthResponse(accessToken, rawRefreshToken, 900, "Bearer");
  }
}
