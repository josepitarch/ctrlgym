package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.exceptions.InvalidTokenException;
import dev.jpitarch.ctrlgym.authentication.repositories.UserRepository;
import dev.jpitarch.ctrlgym.notifications.EmailTemplateComponent;
import dev.jpitarch.ctrlgym.notifications.services.EmailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PasswordRecoveryService {

  private final SecretKey secretKey;

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final EmailTemplateComponent emailTemplateComponent;

  private final EmailService emailService;

  private static final long RESET_TOKEN_EXPIRATION_HOURS = 1;

  public PasswordRecoveryService(
    @Value("${jwt.secret}") String secret,
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    EmailTemplateComponent emailTemplateComponent,
    EmailService emailService
  ) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailTemplateComponent = emailTemplateComponent;
    this.emailService = emailService;
  }

  public void forgotPassword(String email) {
    var user = userRepository.findByEmail(email)
      .orElse(null);

    if (user == null) {
      log.info("Password reset requested for non-existing email {}", email);
      return;
    }

    var now = Instant.now();
    String token = Jwts.builder()
      .subject(user.getId().toString())
      .claim("type", "password_reset")
      .claim("email", email)
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(RESET_TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS)))
      .signWith(secretKey)
      .compact();

    String template = emailTemplateComponent.build("password-reset.html",
      Map.of("ResetURL", "https://app.ctrlgym.es/reset-password" + "?token=" + token));
    emailService.send(email, "Recuperar contraseña - CtrlGym", template);
  }

  public void resetPassword(String token, String newPassword) {
    var claims = Jwts.parser()
      .verifyWith(secretKey)
      .build()
      .parseSignedClaims(token)
      .getPayload();

    String type = claims.get("type", String.class);
    if (!"password_reset".equals(type)) {
      throw new InvalidTokenException("Token no es válido para recuperación de contraseña");
    }

    String userId = claims.getSubject();
    var user = userRepository.findById(UUID.fromString(userId));
    if (user == null) {
      throw new InvalidTokenException("Usuario no encontrado");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    log.info("Password reset successfully for user {}", userId);
  }
}
