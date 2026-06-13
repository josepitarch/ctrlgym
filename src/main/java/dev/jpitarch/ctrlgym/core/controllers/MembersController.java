package dev.jpitarch.ctrlgym.core.controllers;

import com.google.zxing.WriterException;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.services.MembersService;
import dev.jpitarch.ctrlgym.core.services.RoutinesService;
import dev.jpitarch.ctrlgym.core.services.WorkoutsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/members")
@RequiredArgsConstructor
public class MembersController {

  private final MembersService membersService;

  private final WorkoutsService workoutsService;

  private final RoutinesService routinesService;

  @GetMapping(value = "/{memberId}/accesses")
  public List<MemberAccess> getAccesses(@PathVariable UUID memberId) {
    return membersService.getAccesses(memberId);
  }

  @PostMapping("/{memberId}/routines")
  public ResponseEntity<Void> create(@PathVariable UUID memberId, @RequestBody RoutineRequest request) {
    Routine routine = request.routine();
    routinesService.create(routine, memberId);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(value = "/{memberId}/routines")
  public Page<Routine> getRoutines(@PathVariable UUID memberId, Pageable pageable) {
    return routinesService.getRoutines(memberId, pageable);
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
    byte[] qrImage = membersService.generateQrCode(memberId, gymId);
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
  }

  @GetMapping(value = "/{memberId}/invoices/{invoiceId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getInvoiceReport(@PathVariable UUID memberId, @PathVariable UUID invoiceId) throws IOException {
    byte[] pdfReport = membersService.getInvoiceReport(memberId, invoiceId);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfReport);
  }

  public record RoutineRequest(Routine routine, UUID memberId, Integer gymId) {
  }


}
