package dev.jpitarch.ctrlgym.core.usecases;

import com.google.zxing.WriterException;
import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MissingMandatoryAcceptanceException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.StaleLegalDocumentException;
import dev.jpitarch.ctrlgym.core.entities.MemberTermsAcceptanceEntity;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.repositories.LegalDocumentsRepository;
import dev.jpitarch.ctrlgym.core.services.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

  private final LegalDocumentsRepository legalDocumentsRepository;

  private static final Set<LegalDocumentType> MANDATORY_TYPES =
    Set.of(LegalDocumentType.TERMS_OF_SERVICE, LegalDocumentType.PRIVACY_POLICY);

  @Transactional
  public void createMember(Member member, List<UUID> acceptedDocumentVersionIds, String ip, String userAgent) throws StripeException {

    List<LegalDocumentVersion> acceptedVersions = legalDocumentsRepository.findAllById(acceptedDocumentVersionIds);

    for (LegalDocumentVersion version : acceptedVersions) {
      if (!version.isActive()) {
        throw new StaleLegalDocumentException(version.getType());
      }
    }

    Set<LegalDocumentType> acceptedTypes = acceptedVersions.stream()
      .map(LegalDocumentVersion::getType)
      .collect(Collectors.toSet());

    for (LegalDocumentType mandatory : MANDATORY_TYPES) {
      if (!acceptedTypes.contains(mandatory)) {
        throw new MissingMandatoryAcceptanceException(mandatory);
      }
    }

    membersService.create(member);

    for (LegalDocumentVersion version : acceptedVersions) {
      MemberTermsAcceptanceEntity acceptance = new MemberTermsAcceptanceEntity();
      acceptance.setId(UUID.randomUUID());
      acceptance.setMemberId(member.getId());
      acceptance.setDocumentVersionId(version.getId());
      acceptance.setAcceptedAt(OffsetDateTime.now());
      acceptance.setIpAddress(ip);
      acceptance.setUserAgent(userAgent);
      legalDocumentsRepository.saveAcceptance(acceptance);
    }
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
