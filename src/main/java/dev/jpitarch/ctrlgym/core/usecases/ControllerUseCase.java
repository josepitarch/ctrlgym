package dev.jpitarch.ctrlgym.core.usecases;

import dev.jpitarch.ctrlgym.core.dto.Heartbeat;
import dev.jpitarch.ctrlgym.core.models.GymBranchHeartbeatMO;
import dev.jpitarch.ctrlgym.core.models.MemberAccessMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.GymHeartbeatJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
    GymBranchHeartbeatMO latest = gymHeartbeatJpaRepository
      .findTopByGymBranchIdOrderByCreatedAtDesc(gymBranchId)
      .orElse(null);

    if (latest == null) return null;

    double cpuPercent = latest.getCpuPercent() != null ? latest.getCpuPercent().doubleValue() : 0.0;
    double temperature = latest.getTemperatureC() != null ? latest.getTemperatureC().doubleValue() : 0.0;

    var rateWindowStart = OffsetDateTime.now().truncatedTo(ChronoUnit.HOURS).minusHours(WINDOWS_RATE_HOURS);
    long countLastWindow = gymHeartbeatJpaRepository.countByGymBranchIdSince(gymBranchId, rateWindowStart);

    double rate = Math.round((double) countLastWindow / RATE_EMIT_INTERVAL_SECONDS * 1000.0) / 10.0;

    return new Heartbeat(rate, cpuPercent, temperature);
  }

}
