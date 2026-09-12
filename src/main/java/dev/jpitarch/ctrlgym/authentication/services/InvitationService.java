package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.dtos.AuthResponse;
import dev.jpitarch.ctrlgym.authentication.exceptions.InvalidTokenException;
import dev.jpitarch.ctrlgym.authentication.repositories.UserRepository;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.events.EmployeeCreatedEvent;
import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import dev.jpitarch.ctrlgym.notifications.EmailTemplateComponent;
import dev.jpitarch.ctrlgym.notifications.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Service
public class InvitationService {

  private final String baseUrl;

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtFactory jwtFactory;

  private final RefreshTokenService refreshTokenService;

  private final EmailTemplateComponent emailTemplateComponent;

  private final EmailService emailService;

  private final GymsRepository gymsRepository;

  public InvitationService(
    @Value("${email.redirect.base-url}") String baseUrl,
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtFactory jwtFactory,
    RefreshTokenService refreshTokenService,
    EmailTemplateComponent emailTemplateComponent,
    EmailService emailService,
    GymsRepository gymsRepository
  ) {
    this.baseUrl = baseUrl;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtFactory = jwtFactory;
    this.refreshTokenService = refreshTokenService;
    this.emailTemplateComponent = emailTemplateComponent;
    this.emailService = emailService;
    this.gymsRepository = gymsRepository;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void generateInvitationToken(EmployeeCreatedEvent event) {
    log.info("Generating invitation token for email {}...", event.getEmail());

    String token = jwtFactory.generateInvitationToken(event.getEmail(), event.getGymId());

    var gym = gymsRepository.getById(event.getGymId());

    String template = emailTemplateComponent.build("employee-invitation.html", Map.of(
      "ConfirmationURL", baseUrl + "/signup" + "?token=" + token,
      "GymName", gym.getName()
    ));
    emailService.send(event.getEmail(), "Alta empleado en " + gym.getName(), template);
  }

  public AuthResponse acceptInvitation(String token, String password) throws InvalidTokenException {
    JwtFactory.InvitationPayload payload = jwtFactory.parseInvitationToken(token);

    var user = userRepository.findByEmail(payload.email())
      .orElseThrow(() -> new InvalidTokenException("Usuario no encontrado"));

    if (!user.getStatus().equals(UserStatus.PENDING_ACTIVATION)) {
      throw new InvalidTokenException("Usuario ya activado o no válido para activación");
    }

    log.info("Activating user with id {} for gym with id {}...", user.getId(), payload.gymId());

    user.setPassword(passwordEncoder.encode(password));
    user.setStatus(UserStatus.ACTIVE);
    userRepository.save(user);

    String accessToken = jwtFactory.generateAccessToken(user);
    String rawRefreshToken = refreshTokenService.generateRawRefreshToken(user.getId(), user.getGymId());

    return new AuthResponse(accessToken, rawRefreshToken, 900, "Bearer");
  }

}
