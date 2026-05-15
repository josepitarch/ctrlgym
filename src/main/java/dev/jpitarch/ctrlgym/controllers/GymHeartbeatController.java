package dev.jpitarch.ctrlgym.controllers;

import dev.jpitarch.ctrlgym.models.GymBranchHeartbeat;
import dev.jpitarch.ctrlgym.repositories.jpa.GymHeartbeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
public class GymHeartbeatController {

  private final GymHeartbeatJpaRepository jpaRepository;

  @PostMapping("/{gymId}/heartbeat")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void saveHeartbeat(@PathVariable Integer gymId, @RequestBody GymBranchHeartbeat heartbeat) {
    heartbeat.setGymId(gymId);
    heartbeat.setReceivedAt(OffsetDateTime.now());
    jpaRepository.save(heartbeat);
  }

}
