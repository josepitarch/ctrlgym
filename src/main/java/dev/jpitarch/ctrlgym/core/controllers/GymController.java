package dev.jpitarch.ctrlgym.core.controllers;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.dto.CreateMembershipPlanRequest;
import dev.jpitarch.ctrlgym.core.dto.CurrentOccupancy;
import dev.jpitarch.ctrlgym.core.dto.MemberRetention;
import dev.jpitarch.ctrlgym.core.usecases.GymUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/gyms")
public class GymController {

  private final GymUseCase useCase;

  @GetMapping("/{gymId}/branches")
  public ResponseEntity<List<GymBranch>> getBranches(@PathVariable Integer gymId) {
    return ResponseEntity.ok(useCase.getBranches(gymId));
  }

  @GetMapping("/{gymId}/branches/{branchId}/members")
  public ResponseEntity<List<Member>> getMembers(@PathVariable int gymId, @PathVariable int branchId) {
    return ResponseEntity.ok(useCase.getMembers(GymBranchId.of(gymId, branchId)));
  }

  @GetMapping("/{gymId}/branches/{branchId}/members/{memberId}/retention")
  public ResponseEntity<MemberRetention> getMemberRetention(@PathVariable int gymId, @PathVariable int branchId, @PathVariable UUID memberId) {
    return ResponseEntity.ok(useCase.getMemberRetention(GymBranchId.of(gymId, branchId), Member.Id.of(memberId, gymId)));
  }

  @GetMapping("/{gymId}/branches/{branchId}/occupancy")
  public ResponseEntity<CurrentOccupancy> getCurrentOccupancy(@PathVariable Integer gymId, @PathVariable Integer branchId) {
    return ResponseEntity.ok(useCase.getCurrentOccupancy(GymBranchId.of(gymId, branchId)));
  }

  @PostMapping("/{gymId}/exercises")
  public ResponseEntity<Exercise> createExercise(@PathVariable Integer gymId, @RequestBody Exercise exercise) {
    return ResponseEntity.status(HttpStatus.CREATED).body(useCase.createExercise(gymId, exercise));
  }

  @GetMapping("/{gymId}/exercises")
  public ResponseEntity<List<Exercise>> getExercises(@PathVariable Integer gymId) {
    return ResponseEntity.ok(useCase.getAll(gymId));
  }

  @DeleteMapping("/{gymId}/exercises/{exerciseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteExercise(@PathVariable Integer gymId, @PathVariable Integer exerciseId) {
    useCase.deleteExercise(exerciseId, gymId);
  }

  @PostMapping("/{gymId}/memberships/plans")
  public ResponseEntity<Void> createMembershipPlan(@PathVariable Integer gymId, @RequestBody CreateMembershipPlanRequest request) throws StripeException {
    useCase.createMembershipPlan(gymId, request);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{gymId}/memberships/plans")
  public ResponseEntity<List<MembershipPlan>> getMembershipPlans(@PathVariable Integer gymId) {
    return ResponseEntity.ok(useCase.getMembershipPlans(gymId));
  }

  @DeleteMapping("/{gymId}/memberships/plans/{planId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteMembershipPlan(@PathVariable Integer gymId, @PathVariable String planId) throws StripeException {
    useCase.deleteMembershipPlan(planId, gymId);
  }


}
