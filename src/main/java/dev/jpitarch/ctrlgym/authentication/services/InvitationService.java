package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.exceptions.InvalidTokenException;
import dev.jpitarch.ctrlgym.core.domain.enums.Role;
import io.jsonwebtoken.JwtException;
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
public class InvitationService {

  private final SecretKey secretKey;

  private static final long INVITATION_TOKEN_EXPIRATION_DAYS = 7;

  public InvitationService(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateInvitationToken(String email, Integer gymId, Role role) {
    var now = Instant.now();
    return Jwts.builder()
      .subject(email)
      .claim("type", "invitation")
      .claim("gymId", gymId.toString())
      .claim("role", role.name())
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(INVITATION_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS)))
      .signWith(secretKey)
      .compact();
  }

  public InvitationPayload validateAndParse(String token) {
    try {
      var claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();

      String type = claims.get("type", String.class);
      if (!"invitation".equals(type)) {
        throw new InvalidTokenException("Token no es una invitación");
      }

      return new InvitationPayload(
        claims.getSubject(),
        Integer.valueOf(claims.get("gymId", String.class)),
        Role.valueOf(claims.get("role", String.class))
      );
    } catch (JwtException e) {
      throw new InvalidTokenException("Token de invitación inválido o expirado");
    }
  }

  public record InvitationPayload(String email, Integer gymId, Role role) {
  }
}
