package dev.jpitarch.ctrlgym.core.notifications;

import dev.jpitarch.ctrlgym.core.events.ExceptionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class TelegramNotificationService {

  private final String botToken;
  private final String chatId;
  private final RestClient restClient;

  public TelegramNotificationService(
    @Value("${notifications.telegram.bot.token:}") String botToken,
    @Value("${notifications.telegram.chat.id:}") String chatId
  ) {
    this.botToken = botToken;
    this.chatId = chatId;
    this.restClient = RestClient.builder().build();
  }

  public void sendExceptionNotification(ExceptionEvent event) {
    if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
      log.debug("Telegram notification disabled: missing bot token or chat id");
      return;
    }

    String message = formatMessage(event);
    sendTelegramMessage(message);
  }

  private String formatMessage(ExceptionEvent event) {
    return """
      🚨 *Excepción capturada*
      
      *Tipo:* %s
      *Status:* %d
      *Mensaje:* %s
      *Endpoint:* %s %s
      *Timestamp:* %s
      """.formatted(
      event.getExceptionType(),
      event.getStatusCode().value(),
      event.getMessage(),
      event.getHttpMethod(),
      event.getRequestUri(),
      event.getTimestamp()
    );
  }

  private void sendTelegramMessage(String message) {
    try {
      String url = "https://api.telegram.org/bot%s/sendMessage".formatted(botToken);
      
      var requestBody = new java.util.HashMap<String, Object>();
      requestBody.put("chat_id", chatId);
      requestBody.put("text", message);
      requestBody.put("parse_mode", "Markdown");

      restClient.post()
        .uri(url)
        .body(requestBody)
        .retrieve()
        .toBodilessEntity();

      log.debug("Telegram notification sent successfully");
    } catch (Exception e) {
      log.error("Failed to send Telegram notification: {}", e.getMessage(), e);
    }
  }
}
