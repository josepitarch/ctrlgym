package dev.jpitarch.ctrlgym.controllers;

import dev.jpitarch.ctrlgym.models.MemberAccess;
import dev.jpitarch.ctrlgym.repositories.jpa.AccessEventJpaRepository;
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

  @PostMapping("/{gymBranchId}/access-events")
  public void uploadAccessEvent(@PathVariable String gymBranchId, @RequestBody MemberAccess memberAccess) {
    memberAccess.setGymBranchId(gymBranchId);
    memberAccess.setReceivedAt(LocalDateTime.now());
    jpaRepository.save(memberAccess);
  }

}
