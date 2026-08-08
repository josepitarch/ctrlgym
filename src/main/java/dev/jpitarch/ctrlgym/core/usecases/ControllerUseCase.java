package dev.jpitarch.ctrlgym.core.usecases;

import dev.jpitarch.ctrlgym.core.dto.Heartbeat;
import dev.jpitarch.ctrlgym.core.models.GymBranchHeartbeatMO;
import dev.jpitarch.ctrlgym.core.models.MemberAccessMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.GymHeartbeatJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ControllerUseCase {

  private static final int WINDOWS_RATE_HOURS = 1;

  private static final int RATE_EMIT_INTERVAL_SECONDS = 90;

  private final GymHeartbeatJpaRepository gymHeartbeatJpaRepository;

  private final MemberAccessJpaRepository memberAccessJpaRepository;

  public void saveHeartbeat(Integer gymBranchId, GymBranchHeartbeatMO heartbeat) {
    heartbeat.setGymBranchId(gymBranchId);
    heartbeat.setReceivedAt(OffsetDateTime.now());
    gymHeartbeatJpaRepository.save(heartbeat);
  }

  public void uploadAccessEvent(Integer gymBranchId, List<MemberAccessMO> memberAccessMO) {
    memberAccessMO.forEach(ma -> {
      ma.setGymBranchId(gymBranchId);
      ma.setReceivedAt(OffsetDateTime.now());
    });
    memberAccessJpaRepository.saveAll(memberAccessMO);
  }

  public Heartbeat getHealth(Integer gymBranchId) {
    var rateWindowStart = OffsetDateTime.now().truncatedTo(ChronoUnit.HOURS).minusHours(WINDOWS_RATE_HOURS);

    GymBranchHeartbeatMO latest = gymHeartbeatJpaRepository
      .findTopByGymBranchIdAndCreatedAtAfterOrderByCreatedAtDesc(gymBranchId, rateWindowStart)
      .orElse(null);

    if (latest == null) return null;

    Double cpuPercent = Optional.ofNullable(latest.getCpuPercent()).map(BigDecimal::doubleValue).orElse(null);
    Double temperature = Optional.ofNullable(latest.getTemperatureC()).map(BigDecimal::doubleValue).orElse(null);

    long countLastWindow = gymHeartbeatJpaRepository.countByGymBranchIdSince(gymBranchId, rateWindowStart);

    double expectedCount = (double) WINDOWS_RATE_HOURS * 3600 / RATE_EMIT_INTERVAL_SECONDS;
    double rate = Math.min(100, Math.round(countLastWindow * 100.0 / expectedCount));

    return new Heartbeat(rate, cpuPercent, temperature);
  }

}
