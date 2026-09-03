package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.config.AccessQrProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateAccessQr {

  private final PrivateKey signingKey;

  private final AccessQrProperties accessQrProperties;

  public String generateEntryToken(UUID memberId, String role, Integer gymId, Integer gymBranchId) {
    return generateToken(memberId, role, gymId, gymBranchId, accessQrProperties.getEntry(), "entry");
  }

  public String generateExitToken(UUID memberId, String role, Integer gymId, Integer gymBranchId) {
    return generateToken(memberId, role,gymId, gymBranchId, accessQrProperties.getExit(), "exit");
  }

  private String generateToken(UUID memberId, String role, Integer gymId, Integer gymBranchId, Duration expiration, String type) {
    var now = Instant.now();
    return Jwts.builder()
      .subject(memberId.toString())
      .claim("gym_id", gymId)
      .claim("gym_branch_id", gymBranchId)
      .claim("role", role)
      .claim("type", type)
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(expiration)))
      .signWith(signingKey, Jwts.SIG.ES256)
      .compact();
  }

}
