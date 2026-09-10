package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.exceptions.InvalidTokenException;
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
public class JwtFactory {

  private final SecretKey secretKey;

  private static final long ACCESS_TOKEN_EXPIRATION_DAYS = 30;

  private static final long INVITATION_TOKEN_EXPIRATION_DAYS = 7;

  private static final long RESET_TOKEN_EXPIRATION_HOURS = 1;

  public JwtFactory(@Value("${jwt.secret}") String secret) {
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

  public String generateInvitationToken(String email, Integer gymId) {
    var now = Instant.now();
    return Jwts.builder()
      .claim("iss", "https://api.ctrlgym.es")
      .subject(email)
      .claim("type", "invitation")
      .claim("gym_id", gymId)
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(INVITATION_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS)))
      .signWith(secretKey)
      .compact();
  }

  public InvitationPayload parseInvitationToken(String token) {
    var claims = Jwts.parser()
      .verifyWith(secretKey)
      .build()
      .parseSignedClaims(token)
      .getPayload();

    String type = claims.get("type", String.class);
    if (!"invitation".equals(type)) {
      throw new InvalidTokenException("Token no es una invitación");
    }

    return new InvitationPayload(claims.getSubject(), claims.get("gym_id", Integer.class));
  }

  public String generatePasswordResetToken(String userId, String email) {
    var now = Instant.now();
    return Jwts.builder()
      .subject(userId)
      .claim("type", "password_reset")
      .claim("email", email)
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(RESET_TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS)))
      .signWith(secretKey)
      .compact();
  }

  public PasswordResetPayload parsePasswordResetToken(String token) {
    var claims = Jwts.parser()
      .verifyWith(secretKey)
      .build()
      .parseSignedClaims(token)
      .getPayload();

    String type = claims.get("type", String.class);
    if (!"password_reset".equals(type)) {
      throw new InvalidTokenException("Token no es válido para recuperación de contraseña");
    }

    return new PasswordResetPayload(claims.getSubject(), claims.get("email", String.class));
  }

  public record InvitationPayload(String email, Integer gymId) {
  }

  public record PasswordResetPayload(String userId, String email) {
  }
}
