package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.dtos.AuthResponse;
import dev.jpitarch.ctrlgym.authentication.exceptions.InvalidTokenException;
import dev.jpitarch.ctrlgym.authentication.entities.RefreshTokenEntity;
import dev.jpitarch.ctrlgym.authentication.repositories.RefreshTokenRepository;
import dev.jpitarch.ctrlgym.authentication.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final JwtService jwtService;

  private final RefreshTokenRepository refreshTokenRepository;

  private final UserRepository userRepository;

  private final SecureRandom secureRandom = new SecureRandom();

  private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 30;
  private static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 900;

  public String generateRawRefreshToken(UUID userId, Integer gymId) {
    var rawToken = generateRawToken();

    var refreshEntity = new RefreshTokenEntity();
    refreshEntity.setUserId(userId);
    refreshEntity.setGymId(gymId);
    refreshEntity.setTokenHash(hashRefreshToken(rawToken));
    refreshEntity.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS));
    refreshTokenRepository.save(refreshEntity);

    return rawToken;
  }

  @Transactional
  public AuthResponse refresh(String rawRefreshToken) {
    String tokenHash = hashRefreshToken(rawRefreshToken);

    RefreshTokenEntity stored = refreshTokenRepository.findByTokenHash(tokenHash)
      .orElseThrow(() -> new InvalidTokenException("Refresh token no encontrado"));

    if (stored.isRevoked()) {
      refreshTokenRepository.revokeAllByUserId(stored.getUserId());
      throw new SecurityException("Token reutilizado. Todas las sesiones han sido revocadas.");
    }

    if (stored.getExpiresAt().isBefore(Instant.now())) {
      throw new InvalidTokenException("Refresh token expirado");
    }

    stored.setRevoked(true);

    String newRawToken = generateRawToken();
    var newEntity = new RefreshTokenEntity();
    newEntity.setUserId(stored.getUserId());
    newEntity.setGymId(stored.getGymId());
    newEntity.setTokenHash(hashRefreshToken(newRawToken));
    newEntity.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS));
    newEntity.setRevoked(false);
    refreshTokenRepository.save(newEntity);

    stored.setReplacedBy(newEntity.getId());
    refreshTokenRepository.save(stored);

    var user = userRepository.findById(stored.getUserId(), stored.getGymId());
    if (user == null) {
      throw new InvalidTokenException("Usuario no encontrado");
    }

    String newAccessToken = jwtService.generateAccessToken(user);

    return new AuthResponse(newAccessToken, newRawToken, (int) ACCESS_TOKEN_EXPIRATION_SECONDS, "Bearer");
  }

  private String hashRefreshToken(String rawToken) {
    try {
      var digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public void logout(String rawRefreshToken) {
    String tokenHash = hashRefreshToken(rawRefreshToken);

    RefreshTokenEntity stored = refreshTokenRepository.findByTokenHash(tokenHash)
      .orElseThrow(() -> new InvalidTokenException("Refresh token no encontrado"));

    stored.setRevoked(true);
    refreshTokenRepository.save(stored);
  }

  private String generateRawToken() {
    var bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
