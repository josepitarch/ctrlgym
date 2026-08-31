package dev.jpitarch.ctrlgym.core.usecases;

import com.google.zxing.WriterException;
import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.services.*;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberUseCase {

  private final MembersService membersService;

  private final MembershipService membershipService;

  private final WorkoutsService workoutsService;

  private final RoutinesService routinesService;

  private final InvoiceRepository invoiceRepository;

  private final GenerateInvoiceReportService generateInvoiceReportService;

  public void createMember(Member member) throws StripeException {
    membersService.create(member);
  }

  public Member getMember(UUID memberId) {
    return membersService.getMember(memberId);
  }

  public Membership initializeMembership(UUID memberId, Integer gymId, String membershipPlanId) throws StripeException {
    return membershipService.initialize(memberId, gymId, membershipPlanId);
  }

  @SneakyThrows
  public void changeMembership(UUID memberId, Integer gymId, String newMembershipPlanId) {
    membershipService.change(memberId, gymId, newMembershipPlanId);
  }

  public void cancelMembership(UUID memberId, Integer gymId, Long membershipId, Integer cancellationReasonId, String comment) throws StripeException {
    membershipService.cancel(memberId, gymId, membershipId, cancellationReasonId, comment);
  }

  public Optional<Membership> getMembership(UUID memberId) {
    return membershipService.retrieve(memberId);
  }

  public List<MemberAccess> getAccesses(UUID memberId) {
    return membersService.getAccesses(memberId);
  }

  public Map<LocalDate, Boolean> getAttendanceSummary(UUID memberId, LocalDate from, LocalDate to) {
    return membersService.getAttendanceSummary(memberId, from, to);
  }

  public void createRoutine(Routine routine, UUID memberId, Integer gymId) {
    routinesService.create(routine, memberId, gymId);
  }

  public List<Routine> getRoutines(UUID memberId) {
    return routinesService.getRoutines(memberId);
  }

  public void deleteRoutine(Integer routineId, UUID memberId) {
    routinesService.delete(routineId, memberId);
  }

  public void createWorkout(Workout workout, UUID memberId) {
    workoutsService.create(workout, memberId);
  }

  public Page<Workout> getWorkouts(UUID memberId, Pageable pageable) {
    return workoutsService.getWorkouts(memberId, pageable);
  }

  public Page<Invoice> getInvoices(UUID memberId, Pageable pageable) {
    return invoiceRepository.findByMemberId(memberId, pageable);
  }


  public byte[] generateQrCode(UUID memberId, Integer gymId) throws WriterException, IOException {
    return membersService.generateQrCode(memberId, gymId);
  }

  public byte[] getInvoiceReport(UUID memberId, String invoiceId) throws IOException {
    log.info("Generating invoice report for member {} and invoice {}...", memberId, invoiceId);
    return generateInvoiceReportService.generate(memberId, invoiceId);
  }
}
