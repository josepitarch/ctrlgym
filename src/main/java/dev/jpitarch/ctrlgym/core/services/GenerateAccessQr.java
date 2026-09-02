package dev.jpitarch.ctrlgym.core.services;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateAccessQr {

  private final PrivateKey signingKey;

  @Value("${member-access-qr.expiration-seconds:10}")
  private int expirationSeconds;

  private static final int EXIT_TOKEN_EXPIRATION_HOURS = 24;

  public String generateEntryToken(UUID memberId, String role, List<Integer> branches) {
    return generateToken(memberId, role, branches, expirationSeconds, "entry");
  }

  public String generateExitToken(UUID memberId, String role, List<Integer> branches) {
    return generateToken(memberId, role, branches, EXIT_TOKEN_EXPIRATION_HOURS * 3600, "exit");
  }

  private String generateToken(UUID memberId, String role, List<Integer> gymIds, int expirationInSeconds, String type) {
    var now = Instant.now();
    return Jwts.builder()
      .subject(memberId.toString())
      .claim("gym_branches", gymIds)
      .claim("role", role)
      .claim("type", type)
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plusSeconds(expirationInSeconds)))
      .signWith(signingKey, Jwts.SIG.ES256)
      .compact();
  }

}
