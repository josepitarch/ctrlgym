package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.dtos.AuthResponse;
import dev.jpitarch.ctrlgym.authentication.dtos.SigninRequest;
import dev.jpitarch.ctrlgym.authentication.repositories.UserRepository;
import dev.jpitarch.ctrlgym.core.models.UserMO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtService jwtService;

  private final RefreshTokenService refreshTokenService;

  public AuthResponse login(SigninRequest request) {
    UserMO user = userRepository.findByEmail(request.email());

    if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    String accessToken = jwtService.generateAccessToken(user.getId(), user.getGymId(), user.getRole());
    String rawRefreshToken = refreshTokenService.generateRawRefreshToken(user.getId(), user.getGymId());

    return new AuthResponse(accessToken, rawRefreshToken, 900, "Bearer");
  }
}
