package dev.jpitarch.ctrlgym.core.controllers;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.dto.CurrentOccupancy;
import dev.jpitarch.ctrlgym.core.dto.MemberRetention;
import dev.jpitarch.ctrlgym.core.usecases.GymUseCase;
import dev.jpitarch.ctrlgym.core.dto.InvoiceSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/gyms")
public class GymController {

  private final GymUseCase useCase;

  @PreAuthorize("#gymId == authentication.gymId")
  @GetMapping("/{gymId}/branches")
  public ResponseEntity<List<GymBranch>> getBranches(@PathVariable Integer gymId) {
    return ResponseEntity.ok(useCase.getBranches(gymId));
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE') and #gymId == authentication.gymId")
  @GetMapping("/{gymId}/branches/{branchId}/members")
  public ResponseEntity<List<Member>> getMembers(@PathVariable int gymId, @PathVariable int branchId, @RequestParam(required = false) String q) {
    if (q != null && q.length() < 3) {
      throw new IllegalArgumentException("Query parameter 'q' must have at least 3 characters");
    }
    return ResponseEntity.ok(useCase.getMembers(GymBranchId.of(gymId, branchId), q));
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE') and #gymId == authentication.gymId")
  @GetMapping("/{gymId}/branches/{branchId}/members/{memberId}/retention")
  public ResponseEntity<MemberRetention> getMemberRetention(@PathVariable int gymId, @PathVariable int branchId, @PathVariable UUID memberId) {
    return ResponseEntity.ok(useCase.getMemberRetention(GymBranchId.of(gymId, branchId), Member.Id.of(memberId, gymId)));
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE') and #gymId == authentication.gymId")
  @GetMapping("/{gymId}/branches/{branchId}/members/{memberId}/invoices")
  public ResponseEntity<Page<InvoiceSummary>> getInvoices(@PathVariable Integer gymId, @PathVariable Integer branchId, @PathVariable UUID memberId, Pageable pageable) {
    return ResponseEntity.ok(useCase.getInvoices(GymBranchId.of(gymId, branchId), Member.Id.of(memberId, gymId), pageable)
      .map(invoice -> new InvoiceSummary(
        invoice.getId(),
        invoice.getIssueAt(),
        invoice.getTotal(),
        invoice.getStatus()
      )));
  }

  @PreAuthorize("#gymId == authentication.gymId")
  @GetMapping("/{gymId}/branches/{branchId}/occupancy")
  public ResponseEntity<CurrentOccupancy> getCurrentOccupancy(@PathVariable Integer gymId, @PathVariable Integer branchId) {
    return ResponseEntity.ok(useCase.getCurrentOccupancy(GymBranchId.of(gymId, branchId)));
  }

  @PostMapping(value = "/{gymId}/exercises", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('MANAGER') and #gymId == authentication.gymId")
  public ResponseEntity<Exercise> createExercise(
    @PathVariable Integer gymId,
    @RequestPart("exercise") Exercise exercise,
    @RequestPart(value = "image", required = false) MultipartFile image) {
    return ResponseEntity.status(HttpStatus.CREATED).body(useCase.createExercise(gymId, exercise, image));
  }

  @GetMapping("/{gymId}/exercises")
  @PreAuthorize("#gymId == authentication.gymId")
  public ResponseEntity<List<Exercise>> getExercises(@PathVariable Integer gymId) {
    return ResponseEntity.ok(useCase.getAll(gymId));
  }

  @DeleteMapping("/{gymId}/exercises/{exerciseId}")
  @PreAuthorize("hasRole('MANAGER') and #gymId == authentication.gymId")
  public ResponseEntity<Void> deleteExercise(@PathVariable Integer gymId, @PathVariable Integer exerciseId) {
    useCase.deleteExercise(exerciseId, gymId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/{gymId}/memberships/plans")
  @PreAuthorize("hasRole('MANAGER') and #gymId == authentication.gymId")
  public ResponseEntity<Void> createMembershipPlan(@PathVariable Integer gymId, @RequestBody MembershipPlan plan) throws StripeException {
    useCase.createMembershipPlan(gymId, plan);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{gymId}/memberships/plans")
  @PreAuthorize("#gymId == authentication.gymId")
  public ResponseEntity<List<MembershipPlan>> getMembershipPlans(@PathVariable Integer gymId, @RequestParam(required = false) Integer gymBranchId) {
    return ResponseEntity.ok(useCase.getMembershipPlans(GymBranchId.of(gymId, gymBranchId)));
  }

  @PreAuthorize("hasRole('MANAGER') and #gymId == authentication.gymId")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{gymId}/memberships/plans/{planId}")
  public void deleteMembershipPlan(@PathVariable Integer gymId, @PathVariable String planId) throws StripeException {
    useCase.deleteMembershipPlan(planId, gymId);
  }

  @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER') and #gymId == authentication.gymId")
  @GetMapping(value = "/{gymId}/branches/{branchId}/members/{memberId}/invoices/{invoiceId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getInvoiceReport(@PathVariable Integer gymId, @PathVariable Integer branchId, @PathVariable UUID memberId, @PathVariable String invoiceId) throws IOException {
    byte[] pdfReport = useCase.getMemberInvoiceReport(GymBranchId.of(gymId, branchId), Member.Id.of(memberId, gymId), invoiceId);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdfReport);
  }

  @PostMapping("/{gymId}/routines")
  @PreAuthorize("hasRole('MANAGER') and #gymId == authentication.gymId")
  public ResponseEntity<Void> createRoutine(@PathVariable Integer gymId, @RequestBody Routine routine) {
    useCase.createGymRoutine(gymId, routine);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("/{gymId}/routines")
  public ResponseEntity<List<Routine>> getRoutines(@PathVariable Integer gymId) {
    return ResponseEntity.ok(useCase.getGymRoutines(gymId));
  }

  @DeleteMapping("/{gymId}/routines/{routineId}")
  @PreAuthorize("hasRole('MANAGER') and #gymId == authentication.gymId")
  public ResponseEntity<Void> deleteRoutine(@PathVariable Integer gymId, @PathVariable Integer routineId) {
    useCase.deleteGymRoutine(routineId, gymId);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasRole('MANAGER')")
  @GetMapping(value = "/expenses/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  public ResponseEntity<byte[]> exportExpenses() throws IOException {
    byte[] excelFile = useCase.generateExpensesExcel();
    return ResponseEntity.ok()
      .header("Content-Disposition", "attachment; filename=\"gastos.xlsx\"")
      .body(excelFile);
  }

  @PreAuthorize("hasRole('MANAGER')")
  @GetMapping("/{gymId}/branches/{branchId}/employees")
  public ResponseEntity<List<Employee>> getEmployees(@PathVariable Integer gymId, @PathVariable Integer branchId) {
    return ResponseEntity.ok(useCase.getEmployees(GymBranchId.of(gymId, branchId)));
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE') and #gymId == authentication.gymId")
  @GetMapping("/{gymId}/branches/{branchId}/products")
  public ResponseEntity<List<Product>> getProducts(@PathVariable Integer gymId, @PathVariable Integer branchId) {
    return ResponseEntity.ok(useCase.getProducts(GymBranchId.of(gymId, branchId)));
  }

  @PostMapping(value = "/{gymId}/branches/{branchId}/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('MANAGER') and #gymId == authentication.gymId")
  public ResponseEntity<Product> createProduct(
    @PathVariable Integer gymId,
    @PathVariable Integer branchId,
    @RequestPart("product") Product product,
    @RequestPart(value = "image", required = false) MultipartFile image) {
    return ResponseEntity.status(HttpStatus.CREATED).body(useCase.createProduct(gymId, branchId, product, image));
  }

  @DeleteMapping("/{gymId}/branches/{branchId}/products/{productId}")
  @PreAuthorize("hasRole('MANAGER') and #gymId == authentication.gymId")
  public ResponseEntity<Void> deleteProduct(@PathVariable Integer gymId, @PathVariable Integer branchId, @PathVariable Integer productId) {
    useCase.deleteProduct(productId);
    return ResponseEntity.noContent().build();
  }

}
