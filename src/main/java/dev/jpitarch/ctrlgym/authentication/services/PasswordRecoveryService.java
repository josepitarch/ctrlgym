package dev.jpitarch.ctrlgym.authentication.services;

import dev.jpitarch.ctrlgym.authentication.exceptions.InvalidTokenException;
import dev.jpitarch.ctrlgym.authentication.repositories.UserRepository;
import dev.jpitarch.ctrlgym.notifications.EmailTemplateComponent;
import dev.jpitarch.ctrlgym.notifications.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PasswordRecoveryService {

  private final String baseUrl;

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtFactory jwtFactory;

  private final EmailTemplateComponent emailTemplateComponent;

  private final EmailService emailService;

  public PasswordRecoveryService(
    @Value("${email.redirect.base-url}") String baseUrl,
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtFactory jwtFactory,
    EmailTemplateComponent emailTemplateComponent,
    EmailService emailService
  ) {
    this.baseUrl = baseUrl;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtFactory = jwtFactory;
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

    String token = jwtFactory.generatePasswordResetToken(user.getId().toString(), email);

    String template = emailTemplateComponent.build("password-reset.html",
      Map.of("ResetURL", baseUrl + "/reset-password" + "?token=" + token));
    emailService.send(email, "Recuperar contraseña - CtrlGym", template);
  }

  public void resetPassword(String token, String newPassword) {
    JwtFactory.PasswordResetPayload payload = jwtFactory.parsePasswordResetToken(token);

    var user = userRepository.findById(UUID.fromString(payload.userId()));
    if (user == null) {
      throw new InvalidTokenException("Usuario no encontrado");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    log.info("Password reset successfully for user {}", payload.userId());
  }
}
