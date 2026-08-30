package dev.jpitarch.ctrlgym.notifications.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final Resend resendClient;

  @Value("${resend.from-email}")
  private String fromEmail;

  public void send(String to, String subject, String htmlBody) {
    var params = CreateEmailOptions.builder()
      .from(fromEmail)
      .to(to)
      .subject(subject)
      .html(htmlBody)
      .build();

    try {
      log.info("Sending email to {}...", to);
      resendClient.emails().send(params);
    } catch (ResendException e) {
      throw new RuntimeException("Error enviando email con Resend", e);
    }
  }
}
