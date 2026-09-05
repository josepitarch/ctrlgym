package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.core.entities.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

  private final SecretKey secretKey;

  private static final long ACCESS_TOKEN_EXPIRATION_DAYS = 30;

  public JwtService(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(UserEntity user) {
    var now = Instant.now();
    return Jwts.builder()
      .claim("iss", "https://api.ctrlgym.es")
      .subject(user.getId().toString())
      .claim("email", user.getEmail())
      .claim("name", user.getName())
      .claim("first_surname", user.getFirstSurname())
      .claim("gym_id", user.getGymId())
      .claim("role", user.getRole())
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(ACCESS_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS)))
      .signWith(secretKey)
      .compact();
  }
}
