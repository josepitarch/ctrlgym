package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.dtos.AuthResponse;
import dev.jpitarch.ctrlgym.authentication.exceptions.InvalidTokenException;
import dev.jpitarch.ctrlgym.authentication.repositories.UserRepository;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.events.EmployeeCreatedEvent;
import dev.jpitarch.ctrlgym.notifications.EmailTemplateComponent;
import dev.jpitarch.ctrlgym.notifications.services.EmailService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class InvitationService {

  private final SecretKey secretKey;

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtService jwtService;

  private final RefreshTokenService refreshTokenService;

  private final EmailTemplateComponent emailTemplateComponent;

  private final EmailService emailService;

  private static final long INVITATION_TOKEN_EXPIRATION_DAYS = 7;

  public InvitationService(
    @Value("${jwt.secret}") String secret,
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtService jwtService,
    RefreshTokenService refreshTokenService, EmailTemplateComponent emailTemplateComponent, EmailService emailService
  ) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
    this.emailTemplateComponent = emailTemplateComponent;
    this.emailService = emailService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void generateInvitationToken(EmployeeCreatedEvent event) {
    log.info("Generating invitation token for email {}...", event.getEmail());

    var now = Instant.now();
    String token = Jwts.builder()
      .subject(event.getEmail())
      .claim("type", "invitation")
      .claim("gym_id", event.getGymId())
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(INVITATION_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS)))
      .signWith(secretKey)
      .compact();

    String template = emailTemplateComponent.build("employee-invitation.html", Map.of("ConfirmationURL", "https://app.ctrlgym.es/signup" + "?token=" + token));
    emailService.send(event.getEmail(), "Invitación a CtrlGym", template);
  }

  public AuthResponse acceptInvitation(String token, String password) throws InvalidTokenException {
    InvitationPayload payload = validateAndParse(token);

    var user = userRepository.findByEmail(payload.email())
      .orElseThrow(() -> new InvalidTokenException("Usuario no encontrado"));

    if(!user.getStatus().equals(UserStatus.PENDING_ACTIVATION)) {
      throw new InvalidTokenException("Usuario ya activado o no válido para activación");
    }

    log.info("Activating user with id {} for gym with id {}...", user.getId(), payload.gymId());

    user.setPassword(passwordEncoder.encode(password));
    user.setStatus(UserStatus.ACTIVE);
    userRepository.save(user);

    String accessToken = jwtService.generateAccessToken(user);
    String rawRefreshToken = refreshTokenService.generateRawRefreshToken(user.getId(), user.getGymId());

    return new AuthResponse(accessToken, rawRefreshToken, 900, "Bearer");
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

      return new InvitationPayload(claims.getSubject(), Integer.valueOf(claims.get("gym_id", String.class)));
    } catch (JwtException e) {
      throw new InvalidTokenException("Token de invitación inválido o expirado");
    }
  }

  public record InvitationPayload(String email, Integer gymId) {
  }

}
