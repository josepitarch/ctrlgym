package dev.jpitarch.ctrlgym.controllers;

import dev.jpitarch.ctrlgym.models.DeviceHeartbeat;
import dev.jpitarch.ctrlgym.repositories.DeviceHeartbeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DeviceHeartbeatController {

  private final DeviceHeartbeatJpaRepository jpaRepository;

  @PostMapping("/{deviceId}/heartbeat")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void saveHeartbeat(@PathVariable String deviceId, @RequestBody DeviceHeartbeat heartbeat) {
    heartbeat.setDeviceId(deviceId);
    jpaRepository.save(heartbeat);
  }

}
