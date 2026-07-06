package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.domain.MembershipCancellationReason;
import dev.jpitarch.ctrlgym.core.services.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/memberships/cancellation-reasons")
@RequiredArgsConstructor
public class MembershipController {

  private final MembershipService membershipService;

  @GetMapping
  public ResponseEntity<List<MembershipCancellationReason>> getAll() {
    return ResponseEntity.ok(membershipService.getCancellationReasons());
  }
}
