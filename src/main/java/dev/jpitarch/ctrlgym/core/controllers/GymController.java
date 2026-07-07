package dev.jpitarch.ctrlgym.core.controllers;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.dto.CreateMembershipPlanRequest;
import dev.jpitarch.ctrlgym.core.dto.CurrentOccupancy;
import dev.jpitarch.ctrlgym.core.models.GymBranchHeartbeatMO;
import dev.jpitarch.ctrlgym.core.models.MemberAccessMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.GymHeartbeatJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import dev.jpitarch.ctrlgym.core.usecases.GymUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/gyms")
public class GymController {

  private final GymUseCase useCase;

  private final GymHeartbeatJpaRepository gymHeartbeatJpaRepository;

  private final MemberAccessJpaRepository memberAccessJpaRepository;

  @GetMapping("/{gymId}/branches/{branchId}/occupancy")
  public ResponseEntity<CurrentOccupancy> getCurrentOccupancy(@PathVariable Integer gymId, @PathVariable Integer branchId) {
    return ResponseEntity.ok(useCase.getCurrentOccupancy(GymBranchId.of(gymId, branchId)));
  }


  @GetMapping("/{gymId}/exercises")
  public ResponseEntity<List<Exercise>> getExercises(@PathVariable Integer gymId) {
    return ResponseEntity.ok(useCase.getAll(gymId));
  }

  @PostMapping("/{gymId}/memberships/plans")
  public ResponseEntity<Void> createMembershipPlan(@PathVariable Integer gymId, @RequestBody CreateMembershipPlanRequest request) throws StripeException {
    useCase.createMembershipPlan(gymId, request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{gymId}/branches/{gymBranchId}/heartbeat")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void saveHeartbeat(@PathVariable Integer gymId, @PathVariable Integer gymBranchId, @RequestBody GymBranchHeartbeatMO heartbeat) {
    heartbeat.setGymBranchId(gymBranchId);
    heartbeat.setReceivedAt(OffsetDateTime.now());
    gymHeartbeatJpaRepository.save(heartbeat);
  }

  @PostMapping("/{gymId}/branches/{gymBranchId}/access-events")
  public void uploadAccessEvent(@PathVariable Integer gymId, @PathVariable Integer gymBranchId, @RequestBody MemberAccessMO memberAccessMO) {
    memberAccessMO.setGymBranchId(gymBranchId);
    memberAccessMO.setReceivedAt(OffsetDateTime.now());
    memberAccessJpaRepository.save(memberAccessMO);
  }


}
