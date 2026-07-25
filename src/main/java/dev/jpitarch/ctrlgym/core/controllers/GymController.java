package dev.jpitarch.ctrlgym.core.controllers;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.dto.CurrentOccupancy;
import dev.jpitarch.ctrlgym.core.dto.MemberRetention;
import dev.jpitarch.ctrlgym.core.usecases.GymUseCase;
import dev.jpitarch.ctrlgym.payments.dto.InvoiceSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

  @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE')")
  @GetMapping("/{gymId}/branches/{branchId}/members")
  public ResponseEntity<List<Member>> getMembers(@PathVariable int gymId, @PathVariable int branchId) {
    return ResponseEntity.ok(useCase.getMembers(GymBranchId.of(gymId, branchId)));
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE')")
  @GetMapping("/{gymId}/branches/{branchId}/members/{memberId}/retention")
  public ResponseEntity<MemberRetention> getMemberRetention(@PathVariable int gymId, @PathVariable int branchId, @PathVariable UUID memberId) {
    return ResponseEntity.ok(useCase.getMemberRetention(GymBranchId.of(gymId, branchId), Member.Id.of(memberId, gymId)));
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE')")
  @GetMapping("/{gymId}/branches/{branchId}/members/{memberId}/invoices")
  public ResponseEntity<Page<InvoiceSummary>> getInvoices(@PathVariable Integer gymId, @PathVariable Integer branchId, @PathVariable UUID memberId, Pageable pageable) {
    return ResponseEntity.ok(useCase.getInvoices(GymBranchId.of(gymId, branchId), Member.Id.of(memberId, gymId), pageable)
      .map(invoice -> new InvoiceSummary(
        invoice.getId(),
        invoice.getIssueAt(),
        null,
        invoice.getTotal()
      )));
  }

  @GetMapping("/{gymId}/branches/{branchId}/occupancy")
  public ResponseEntity<CurrentOccupancy> getCurrentOccupancy(@PathVariable Integer gymId, @PathVariable Integer branchId) {
    return ResponseEntity.ok(useCase.getCurrentOccupancy(GymBranchId.of(gymId, branchId)));
  }

  @PreAuthorize("hasRole('MANAGER')")
  @PostMapping("/{gymId}/exercises")
  public ResponseEntity<Exercise> createExercise(@PathVariable Integer gymId, @RequestBody Exercise exercise) {
    return ResponseEntity.status(HttpStatus.CREATED).body(useCase.createExercise(gymId, exercise));
  }

  @GetMapping("/{gymId}/exercises")
  public ResponseEntity<List<Exercise>> getExercises(@PathVariable Integer gymId) {
    return ResponseEntity.ok(useCase.getAll(gymId));
  }

  @PreAuthorize("hasRole('MANAGER')")
  @DeleteMapping("/{gymId}/exercises/{exerciseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteExercise(@PathVariable Integer gymId, @PathVariable Integer exerciseId) {
    useCase.deleteExercise(exerciseId, gymId);
  }

  @PreAuthorize("hasRole('MANAGER')")
  @PostMapping("/{gymId}/memberships/plans")
  public ResponseEntity<Void> createMembershipPlan(@PathVariable Integer gymId, @RequestBody MembershipPlan plan) throws StripeException {
    useCase.createMembershipPlan(gymId, plan);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{gymId}/memberships/plans")
  public ResponseEntity<List<MembershipPlan>> getMembershipPlans(@PathVariable Integer gymId, @RequestParam(required = false) Integer gymBranchId) {
    return ResponseEntity.ok(useCase.getMembershipPlans(GymBranchId.of(gymId, gymBranchId)));
  }

  @PreAuthorize("hasRole('MANAGER')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{gymId}/memberships/plans/{planId}")
  public void deleteMembershipPlan(@PathVariable Integer gymId, @PathVariable String planId) throws StripeException {
    useCase.deleteMembershipPlan(planId, gymId);
  }

  @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
  @GetMapping(value = "/{gymId}/branches/{branchId}/members/{memberId}/invoices/{invoiceId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getInvoiceReport(@PathVariable Integer gymId, @PathVariable Integer branchId, @PathVariable UUID memberId, @PathVariable String invoiceId) throws IOException {
    byte[] pdfReport = useCase.getMemberInvoiceReport(GymBranchId.of(gymId, branchId), Member.Id.of(memberId, gymId), invoiceId);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfReport);
  }


}
