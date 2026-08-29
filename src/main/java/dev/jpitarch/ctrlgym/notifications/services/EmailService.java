package dev.jpitarch.ctrlgym.notifications.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final Resend resendClient;

  @Value("${resend.from-email}")
  private String fromEmail;
  public String send(String to, String subject, String htmlBody) {
    var params = CreateEmailOptions.builder()
      .from(fromEmail)
      .to(to)
      .subject(subject)
      .html(htmlBody)
      .build();

    try {
      CreateEmailResponse response = resendClient.emails().send(params);
      return response.getId();
    } catch (ResendException e) {
      throw new RuntimeException("Error enviando email con Resend", e);
    }
  }
}
