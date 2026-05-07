package dev.jpitarch.ctrlgym.controllers;

import dev.jpitarch.ctrlgym.models.GymHeartbeat;
import dev.jpitarch.ctrlgym.repositories.GymHeartbeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
public class GymHeartbeatController {

  private final GymHeartbeatJpaRepository jpaRepository;

  @PostMapping("/{deviceId}/heartbeat")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void saveHeartbeat(@PathVariable String deviceId, @RequestBody GymHeartbeat heartbeat) {
    heartbeat.setDeviceId(deviceId);
    heartbeat.setReceivedAt(OffsetDateTime.now());
    jpaRepository.save(heartbeat);
  }

}
