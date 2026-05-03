package dev.jpitarch.ctrlgym.controllers;

import dev.jpitarch.ctrlgym.models.AccessEvent;
import dev.jpitarch.ctrlgym.repositories.AccessEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class UploadAccessEventController {

  private final AccessEventJpaRepository jpaRepository;

  @PostMapping("/{deviceId}/access-events")
  public void uploadAccessEvent(@PathVariable String deviceId, @RequestBody AccessEvent accessEvent) {
    accessEvent.setDeviceId(deviceId);
    accessEvent.setReceivedAt(LocalDateTime.now());
    jpaRepository.save(accessEvent);
  }

}
