package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.models.GymBranchHeartbeat;
import dev.jpitarch.ctrlgym.core.models.MemberAccess;
import dev.jpitarch.ctrlgym.core.repositories.jpa.GymHeartbeatJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
public class RaspiController {

  private final GymHeartbeatJpaRepository gymHeartbeatJpaRepository;

  private final MemberAccessJpaRepository memberAccessJpaRepository;

  @PostMapping("/gyms/{gymId}/branches/{gymBranchId}/heartbeat")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void saveHeartbeat(@PathVariable Integer gymId, @PathVariable Integer gymBranchId, @RequestBody GymBranchHeartbeat heartbeat) {
    heartbeat.setGymBranchId(gymBranchId);
    heartbeat.setReceivedAt(OffsetDateTime.now());
    gymHeartbeatJpaRepository.save(heartbeat);
  }

  @PostMapping("/gyms/{gymId}/branches/{gymBranchId}/access-events")
  public void uploadAccessEvent(@PathVariable Integer gymId, @PathVariable Integer gymBranchId, @RequestBody MemberAccess memberAccess) {
    memberAccess.setGymBranchId(gymBranchId);
    memberAccess.setReceivedAt(OffsetDateTime.now());
    memberAccessJpaRepository.save(memberAccess);
  }

}
