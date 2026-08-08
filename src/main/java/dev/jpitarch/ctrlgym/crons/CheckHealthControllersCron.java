package dev.jpitarch.ctrlgym.crons;

import dev.jpitarch.ctrlgym.core.dto.Heartbeat;
import dev.jpitarch.ctrlgym.core.usecases.ControllerUseCase;
import dev.jpitarch.ctrlgym.notifications.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class CheckHealthControllersCron {

  private static final double MAX_CPU_PERCENT = 80.0;
  private static final double MAX_TEMPERATURE = 70.0;
  private static final double MIN_RATE_PERCENT = 80.0;

  private final ControllerUseCase controllerUseCase;

  private final TelegramNotificationService notificationService;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Scheduled(cron = "0 0/30 * * * *", zone = "Europe/Madrid")
  public void checkHealth() {
    List<Map<String, Object>> branches = jdbcTemplate.queryForList(
      "SELECT id, name FROM gym_branches",
      Collections.emptyMap()
    );

    for (var branch : branches) {
      Integer branchId = (Integer) branch.get("id");
      String branchName = (String) branch.get("name");

      try {
        Heartbeat heartbeat = controllerUseCase.getHealth(branchId);
        if (heartbeat == null) {
          notificationService.send("⚠️ Controlador de *%s* sin heartbeats en la última hora".formatted(branchName));
          continue;
        }

        if (heartbeat.rate() < MIN_RATE_PERCENT) {
          notificationService.send(
            "⚠️ Controlador de *%s*: tasa de heartbeats baja (%.1f%%)".formatted(branchName, heartbeat.rate())
          );
        }

        if (heartbeat.cpuPercent() != null && heartbeat.cpuPercent() > MAX_CPU_PERCENT) {
          notificationService.send(
            "🔥 Controlador de *%s*: CPU al %.1f%%".formatted(branchName, heartbeat.cpuPercent())
          );
        }

        if (heartbeat.temperature() != null && heartbeat.temperature() > MAX_TEMPERATURE) {
          notificationService.send(
            "🌡️ Controlador de *%s*: temperatura a %.1f°C".formatted(branchName, heartbeat.temperature())
          );
        }
      } catch (Exception e) {
        log.error("Error checking health for branch {}: {}", branchName, e.getMessage());
      }
    }
  }

}
