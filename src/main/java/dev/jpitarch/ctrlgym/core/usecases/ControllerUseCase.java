package dev.jpitarch.ctrlgym.core.usecases;

import dev.jpitarch.ctrlgym.core.dto.Heartbeat;
import dev.jpitarch.ctrlgym.core.entities.GymBranchHeartbeatEntity;
import dev.jpitarch.ctrlgym.core.entities.MemberAccessEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.GymHeartbeatJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ControllerUseCase {

  private static final int WINDOWS_RATE_HOURS = 1;

  private static final int RATE_EMIT_INTERVAL_SECONDS = 90;

  private final GymHeartbeatJpaRepository gymHeartbeatJpaRepository;

  private final MemberAccessJpaRepository memberAccessJpaRepository;

  public void saveHeartbeat(Integer gymBranchId, GymBranchHeartbeatEntity heartbeat) {
    heartbeat.setGymBranchId(gymBranchId);
    heartbeat.setReceivedAt(OffsetDateTime.now());
    gymHeartbeatJpaRepository.save(heartbeat);
  }

  public void uploadAccessEvent(Integer gymBranchId, List<MemberAccessEntity> MemberAccessEntity) {
    if(CollectionUtils.isEmpty(MemberAccessEntity)) return;

    log.info("Uploading {} access events for branch with id {}", MemberAccessEntity.size(), gymBranchId);

    MemberAccessEntity.forEach(ma -> {
      ma.setGymBranchId(gymBranchId);
      ma.setReceivedAt(OffsetDateTime.now());
    });

    memberAccessJpaRepository.saveAll(MemberAccessEntity);
  }

  public Heartbeat getHealth(Integer gymBranchId) {
    var rateWindowStart = OffsetDateTime.now().truncatedTo(ChronoUnit.HOURS).minusHours(WINDOWS_RATE_HOURS);

    GymBranchHeartbeatEntity latest = gymHeartbeatJpaRepository
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
