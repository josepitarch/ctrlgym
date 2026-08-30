package dev.jpitarch.ctrlgym.crons;

import dev.jpitarch.ctrlgym.authentication.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class RefreshTokenCleanupCron {

  private final RefreshTokenRepository refreshTokenRepository;

  @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Madrid")
  public void cleanupExpiredTokens() {
    int deleted = refreshTokenRepository.deleteExpiredOrRevoked();
    log.info("Refresh tokens cleanup completed. Deleted {} tokens", deleted);
  }
}
