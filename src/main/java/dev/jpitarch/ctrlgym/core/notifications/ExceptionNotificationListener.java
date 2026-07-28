package dev.jpitarch.ctrlgym.core.notifications;

import dev.jpitarch.ctrlgym.core.events.ExceptionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionNotificationListener {

  private final TelegramNotificationService telegramNotificationService;
  private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

  @EventListener
  public void handleExceptionEvent(ExceptionEvent event) {
    virtualThreadExecutor.submit(() -> {
      try {
        telegramNotificationService.sendExceptionNotification(event);
      } catch (Exception e) {
        log.error("Error processing exception notification: {}", e.getMessage(), e);
      }
    });
  }
}
