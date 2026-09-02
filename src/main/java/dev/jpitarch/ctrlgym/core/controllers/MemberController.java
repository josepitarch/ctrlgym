package dev.jpitarch.ctrlgym.core.controllers;

import com.google.zxing.WriterException;
import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.usecases.MemberUseCase;
import dev.jpitarch.ctrlgym.core.dto.CreateMemberRequest;
import dev.jpitarch.ctrlgym.core.dto.InvoiceSummary;
import dev.jpitarch.ctrlgym.lib.RequestHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members")
public class MemberController {

  private final MemberUseCase memberUseCase;

  @PostMapping("/{memberId}")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Void> create(@PathVariable UUID memberId, @RequestBody CreateMemberRequest body, HttpServletRequest request) throws StripeException {

    Member member = Member.builder()
      .id(memberId)
      .name(body.getName())
      .firstSurname(body.getFirstSurname())
      .secondSurname(body.getSecondSurname())
      .gender(body.getGender())
      .birthDate(body.getBirthDate())
      .nif(body.getNif())
      .address(body.getAddress() != null
        ? Member.Address.builder()
          .city(body.getAddress().getCity())
          .postalCode(body.getAddress().getPostalCode())
          .build()
        : null)
      .build();

    memberUseCase.createMember(member, body.getAcceptedDocumentVersionIds(), RequestHelper.extractIp(request), request.getHeader("User-Agent"));
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("/{memberId}")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Member> getMember(@PathVariable UUID memberId) {
    return ResponseEntity.ok(memberUseCase.getMember(memberId));
  }


  @PostMapping("/{memberId}/memberships/{membershipPlanId}")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Membership> initializeMembership(@PathVariable UUID memberId, @PathVariable String membershipPlanId, @RequestParam Integer gymId) throws StripeException {
    var membership = memberUseCase.initializeMembership(memberId, gymId, membershipPlanId);
    return ResponseEntity.ok(membership);
  }

  @PutMapping("/{memberId}/memberships")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Void> changeMembership(@PathVariable UUID memberId, @RequestBody String membershipPlanId, @RequestParam Integer gymId) {
    memberUseCase.changeMembership(memberId, gymId, membershipPlanId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{memberId}/memberships/{membershipId}")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Void> cancelMembership(@PathVariable UUID memberId, @PathVariable Long membershipId, @RequestParam Integer gymId,
                                               @RequestParam Integer cancellationReasonId,
                                               @RequestBody Map<String, String> body
  ) throws StripeException {
    memberUseCase.cancelMembership(memberId, gymId, membershipId, cancellationReasonId, body.get("comment"));
    return ResponseEntity.noContent().build();
  }


  @GetMapping("/{memberId}/memberships")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Optional<Membership>> getMembership(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(memberUseCase.getMembership(memberId));
  }

  @GetMapping(value = "/{memberId}/accesses")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<List<MemberAccess>> getAccesses(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(memberUseCase.getAccesses(memberId));
  }

  @GetMapping(value = "/{memberId}/attendaces/summary")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Map<LocalDate, Boolean>> getAttendanceSummary(@PathVariable UUID memberId, @RequestParam Integer gymId,
                                                                      @RequestParam LocalDate from, @RequestParam(required = false) LocalDate to) {
    return ResponseEntity.ok(memberUseCase.getAttendanceSummary(memberId, from, Optional.ofNullable(to).orElse(LocalDate.now())));
  }

  @PostMapping("/{memberId}/routines")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Routine> create(@PathVariable UUID memberId, @RequestBody Routine routine, @RequestParam Integer gymId) {
    Routine createdRoutine = memberUseCase.createRoutine(routine, memberId, gymId);
    return new ResponseEntity<>(createdRoutine, HttpStatus.CREATED);
  }

  @GetMapping(value = "/{memberId}/routines")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<List<Routine>> getRoutines(@PathVariable UUID memberId, @RequestParam Integer gymId) {
    return ResponseEntity.ok(memberUseCase.getRoutines(memberId));
  }

  @DeleteMapping("/{memberId}/routines/{routineId}")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Void> delete(@PathVariable UUID memberId, @PathVariable Integer routineId, @RequestParam Integer gymId) {
    memberUseCase.deleteRoutine(routineId, memberId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/{memberId}/workouts")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Void> createWorkout(@PathVariable UUID memberId, @RequestBody Workout workout) {
    memberUseCase.createWorkout(workout, memberId);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(value = "/{memberId}/workouts")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Page<Workout>> getWorkouts(@PathVariable UUID memberId, Pageable pageable) {
    return ResponseEntity.ok(memberUseCase.getWorkouts(memberId, pageable));
  }

  @GetMapping("/{memberId}/invoices")
  @PreAuthorize("#memberId.toString() == authentication.name")
  public ResponseEntity<Page<InvoiceSummary>> getInvoices(@PathVariable UUID memberId, @RequestParam Integer gymId, Pageable pageable) {
    return ResponseEntity.ok(memberUseCase.getInvoices(memberId, pageable)
      .map(invoice -> new InvoiceSummary(
        invoice.getId(),
        invoice.getIssueAt(),
        invoice.getTotal(),
        invoice.getStatus()
      )));
  }

  @GetMapping(value = "/{memberId}/invoices/{invoiceId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getInvoiceReport(@PathVariable UUID memberId, @PathVariable String invoiceId, @RequestParam Integer gymId) throws IOException {
    byte[] pdfReport = memberUseCase.getInvoiceReport(memberId, invoiceId);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfReport);
  }



  @PreAuthorize("#memberId.toString() == authentication.name")
  @PostMapping(value = "/{memberId}/generate-qr", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> generateQr(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID memberId, @RequestParam Integer gymId) throws WriterException, IOException {
    byte[] qrImage = memberUseCase.generateQrCode(memberId, gymId);
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);
  }

}
