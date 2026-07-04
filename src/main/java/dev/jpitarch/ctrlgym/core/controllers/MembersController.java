package dev.jpitarch.ctrlgym.core.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.zxing.WriterException;
import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.services.MembersService;
import dev.jpitarch.ctrlgym.core.services.MembershipService;
import dev.jpitarch.ctrlgym.core.services.RoutinesService;
import dev.jpitarch.ctrlgym.core.services.WorkoutsService;
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
public class MembersController {

  private final MembersService membersService;

  private final MembershipService membershipService;

  private final WorkoutsService workoutsService;

  private final RoutinesService routinesService;

  @PostMapping("/{memberId}")
  public ResponseEntity<String> create(@PathVariable UUID memberId, @RequestBody Member member, @RequestParam Integer gymId) throws StripeException {
    member.setId(Member.Id.of(memberId, gymId));
    membersService.create(member);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<Member> getMember(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(membersService.getMember(Member.Id.of(memberId, gymId)));
  }

  @GetMapping("/{memberId}/memberships")
  public ResponseEntity<List<Membership>> getMemberships(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(membershipService.getMemberships(Member.Id.of(memberId, gymId)));
  }

  @GetMapping(value = "/{memberId}/accesses")
  public ResponseEntity<List<MemberAccess>> getAccesses(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(membersService.getAccesses(Member.Id.of(memberId, gymId)));
  }

  @GetMapping(value = "/{memberId}/attendaces/summary")
  public ResponseEntity<Map<LocalDate, Boolean>> getAttendanceSummary(@PathVariable UUID memberId, @RequestParam Integer gymId,
                                                                      @RequestParam LocalDate from, @RequestParam(required = false) LocalDate to) {
    return ResponseEntity.ok(membersService.getAttendanceSummary(Member.Id.of(memberId, gymId), from, Optional.ofNullable(to).orElse(LocalDate.now())));
  }

  @PostMapping("/{memberId}/routines")
  public ResponseEntity<Void> create(@PathVariable UUID memberId, @RequestBody Routine routine, @RequestParam Integer gymId) {
    routinesService.create(routine, Member.Id.of(memberId, gymId));
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(value = "/{memberId}/routines")
  public ResponseEntity<List<Routine>> getRoutines(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(routinesService.getRoutines(Member.Id.of(memberId, gymId)));
  }

  @DeleteMapping("/{memberId}/routines/{routineId}")
  public ResponseEntity<Void> delete(@PathVariable UUID memberId, @PathVariable Integer routineId, @RequestParam Integer gymId) {
    routinesService.delete(routineId, Member.Id.of(memberId, gymId));
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/{memberId}/workouts")
  public ResponseEntity<Void> createWorkout(@PathVariable UUID memberId, @RequestBody Workout workout) {
    workoutsService.create(workout, memberId);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(value = "/{memberId}/workouts")
  public ResponseEntity<Page<Workout>> getWorkouts(@PathVariable UUID memberId, Pageable pageable) {
    return ResponseEntity.ok(workoutsService.getWorkouts(memberId, pageable));
  }

  @PostMapping(value = "/{memberId}/generate-qr", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> generateQr(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID memberId, @RequestParam Integer gymId) throws WriterException, IOException {
    byte[] qrImage = membersService.generateQrCode(Member.Id.of(memberId, gymId));
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
  }
  
}
