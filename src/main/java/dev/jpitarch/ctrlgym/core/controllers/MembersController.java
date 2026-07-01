package dev.jpitarch.ctrlgym.core.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.zxing.WriterException;
import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.services.MembersService;
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

  private final WorkoutsService workoutsService;

  private final RoutinesService routinesService;

  @PostMapping("/{memberId}")
  public ResponseEntity<String> create(@PathVariable UUID memberId, @RequestBody Member member, @RequestParam Integer gymId) throws StripeException {
    member.setId(Member.Id.of(memberId, gymId));
    membersService.create(member);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("/{memberId}")
  public Member getMember(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return membersService.getMember(Member.Id.of(memberId, gymId));
  }

  @GetMapping(value = "/{memberId}/accesses")
  public List<MemberAccess> getAccesses(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return membersService.getAccesses(Member.Id.of(memberId, gymId));
  }

  @GetMapping(value = "/{memberId}/attendaces/summary")
  public Map<LocalDate, Boolean> getAttendanceSummary(@PathVariable UUID memberId, @RequestParam Integer gymId,
                                                      @RequestParam LocalDate from, @RequestParam(required = false) LocalDate to) {
    return membersService.getAttendanceSummary(Member.Id.of(memberId, gymId), from, Optional.ofNullable(to).orElse(LocalDate.now()));
  }

  @PostMapping("/{memberId}/routines")
  public ResponseEntity<Void> create(@PathVariable UUID memberId, @RequestBody RoutineRequest request, @RequestParam Integer gymId) {
    Routine routine = request.routine();
    routinesService.create(routine, Member.Id.of(memberId, gymId));
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(value = "/{memberId}/routines")
  public Page<Routine> getRoutines(@PathVariable UUID memberId, @RequestParam Integer gymId, Pageable pageable) {
    return routinesService.getRoutines(Member.Id.of(memberId, gymId), pageable);
  }

  @DeleteMapping("/{memberId}/routines/{routineId}")
  public ResponseEntity<Void> delete(@PathVariable Integer routineId) {
    routinesService.delete(routineId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/{memberId}/workouts")
  public ResponseEntity<Void> createWorkout(@PathVariable UUID memberId, @RequestBody Workout workout) {
    workoutsService.create(workout, memberId);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(value = "/{memberId}/workouts")
  public Page<Workout> getWorkouts(@PathVariable UUID memberId, Pageable pageable) {
    return workoutsService.getWorkouts(memberId, pageable);
  }

  @PostMapping(value = "/{memberId}/generate-qr", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> generateQr(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID memberId, @RequestParam Integer gymId) throws WriterException, IOException {
    byte[] qrImage = membersService.generateQrCode(Member.Id.of(memberId, gymId));
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
  }

  @GetMapping(value = "/{memberId}/invoices/{invoiceId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getInvoiceReport(@PathVariable UUID memberId, @PathVariable UUID invoiceId, @RequestParam Integer gymId) throws IOException {
    byte[] pdfReport = membersService.getInvoiceReport(Member.Id.of(memberId, gymId), invoiceId);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfReport);
  }

  public record RoutineRequest(Routine routine,
                               @JsonProperty("member_id") UUID memberId,
                               @JsonProperty("gym_id") Integer gymId) {
  }


}
