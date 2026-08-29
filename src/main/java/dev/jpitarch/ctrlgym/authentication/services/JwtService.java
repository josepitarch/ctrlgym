package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.core.domain.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

  private final SecretKey secretKey;

  private static final long ACCESS_TOKEN_EXPIRATION_MINUTES = 15;

  public JwtService(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(UUID userId, Integer gymId, Role role) {
    var now = Instant.now();
    return Jwts.builder()
      .subject(userId.toString())
      .claim("iss", "https://api.ctrlgym.es")
      .claim("gymId", gymId.toString())
      .claim("role", role)
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(ACCESS_TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES)))
      .signWith(secretKey)
      .compact();
  }
}
