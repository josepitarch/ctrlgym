package dev.jpitarch.ctrlgym.core.controllers;

import com.google.zxing.WriterException;
import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.usecases.MemberUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberUseCase memberUseCase;

  @PostMapping("/{memberId}")
  public ResponseEntity<Void> create(@PathVariable UUID memberId, @RequestBody Member member, @RequestParam Integer gymId) throws StripeException {
    member.setId(Member.Id.of(memberId, gymId));
    memberUseCase.createMember(member);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<Member> getMember(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(memberUseCase.getMember(Member.Id.of(memberId, gymId)));
  }

  @PostMapping("/{memberId}/memberships/{membershipId}")
  public ResponseEntity<Void> initializeMembership(@PathVariable UUID memberId, @PathVariable String membershipId, @RequestParam Integer gymId) throws StripeException {
    memberUseCase.initializeMembership(Member.Id.of(memberId, gymId), membershipId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{memberId}/memberships/{membershipId}")
  public ResponseEntity<Void> cancelMembership(@PathVariable UUID memberId, @PathVariable String membershipId, @RequestParam Integer gymId, @RequestParam Integer cancellationReasonId) throws StripeException {
    memberUseCase.cancelMembership(Member.Id.of(memberId, gymId), membershipId, cancellationReasonId);
    return ResponseEntity.noContent().build();
  }


  @GetMapping("/{memberId}/memberships")
  public ResponseEntity<List<Membership>> getMemberships(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(memberUseCase.getMemberships(Member.Id.of(memberId, gymId)));
  }

  @GetMapping(value = "/{memberId}/accesses")
  public ResponseEntity<List<MemberAccess>> getAccesses(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(memberUseCase.getAccesses(Member.Id.of(memberId, gymId)));
  }

  @GetMapping(value = "/{memberId}/attendaces/summary")
  public ResponseEntity<Map<LocalDate, Boolean>> getAttendanceSummary(@PathVariable UUID memberId, @RequestParam Integer gymId,
                                                                      @RequestParam LocalDate from, @RequestParam(required = false) LocalDate to) {
    return ResponseEntity.ok(memberUseCase.getAttendanceSummary(Member.Id.of(memberId, gymId), from, Optional.ofNullable(to).orElse(LocalDate.now())));
  }

  @PostMapping("/{memberId}/routines")
  public ResponseEntity<Void> create(@PathVariable UUID memberId, @RequestBody Routine routine, @RequestParam Integer gymId) {
    memberUseCase.createRoutine(routine, Member.Id.of(memberId, gymId));
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(value = "/{memberId}/routines")
  public ResponseEntity<List<Routine>> getRoutines(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(memberUseCase.getRoutines(Member.Id.of(memberId, gymId)));
  }

  @DeleteMapping("/{memberId}/routines/{routineId}")
  public ResponseEntity<Void> delete(@PathVariable UUID memberId, @PathVariable Integer routineId, @RequestParam Integer gymId) {
    memberUseCase.deleteRoutine(routineId, Member.Id.of(memberId, gymId));
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/{memberId}/workouts")
  public ResponseEntity<Void> createWorkout(@PathVariable UUID memberId, @RequestBody Workout workout) {
    memberUseCase.createWorkout(workout, memberId);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(value = "/{memberId}/workouts")
  public ResponseEntity<Page<Workout>> getWorkouts(@PathVariable UUID memberId, Pageable pageable) {
    return ResponseEntity.ok(memberUseCase.getWorkouts(memberId, pageable));
  }

  @PostMapping(value = "/{memberId}/generate-qr", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> generateQr(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID memberId, @RequestParam Integer gymId) throws WriterException, IOException {
    byte[] qrImage = memberUseCase.generateQrCode(Member.Id.of(memberId, gymId));
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
  }

}
