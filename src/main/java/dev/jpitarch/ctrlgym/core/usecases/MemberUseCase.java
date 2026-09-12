package dev.jpitarch.ctrlgym.core.usecases;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.dto.AccessTokensResponse;
import dev.jpitarch.ctrlgym.core.repositories.InvoiceRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import dev.jpitarch.ctrlgym.core.services.*;
import dev.jpitarch.ctrlgym.payments.services.CustomerService;
import dev.jpitarch.ctrlgym.storage.config.StorageBucket;
import dev.jpitarch.ctrlgym.storage.services.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

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

  private final MembersRepository membersRepository;

  private final StorageService storageService;

  public Member getMember(UUID memberId) {
    Member member = membersService.getMember(memberId);
    return resolveAvatarUrl(member);
  }

  public Membership initializeMembership(UUID memberId, String membershipPlanId) throws StripeException {
    return membershipService.initialize(memberId, TenantContextHolder.getTenantId(), membershipPlanId);
  }

  @SneakyThrows
  public void changeMembership(UUID memberId, String newMembershipPlanId) {
    membershipService.change(memberId, TenantContextHolder.getTenantId(), newMembershipPlanId);
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

  public Routine createRoutine(Routine routine, UUID memberId) {
    return routinesService.create(routine, memberId, TenantContextHolder.getTenantId());
  }

  public List<Routine> getRoutines(UUID memberId) {
    return routinesService.getRoutines(memberId);
  }

  public void deleteRoutine(Integer routineId, UUID memberId) {
    routinesService.delete(routineId, memberId);
  }

  public Workout createWorkout(Workout workout, UUID memberId) {
    return workoutsService.create(workout, memberId);
  }

  public Page<Workout> getWorkouts(UUID memberId, Pageable pageable) {
    return workoutsService.getWorkouts(memberId, pageable);
  }

  public Page<Invoice> getInvoices(UUID memberId, Pageable pageable) {
    return invoiceRepository.findByMemberId(memberId, pageable);
  }


  public AccessTokensResponse generateAccessTokens(UUID memberId) {
    return membersService.generateAccessTokens(memberId);
  }

  public byte[] getInvoiceReport(UUID memberId, String invoiceId) throws IOException {
    log.info("Generating invoice report for member {} and invoice {}...", memberId, invoiceId);
    return generateInvoiceReportService.generate(memberId, invoiceId);
  }

  public String updateAvatar(UUID memberId, MultipartFile file) {
    Integer tenantId = TenantContextHolder.getTenantId();
    String avatarKey = storageService.uploadFile(file, tenantId, "avatars", memberId.toString(), StorageBucket.AVATARS);

    String oldAvatarKey = membersRepository.getAvatarUrl(memberId);
    if (oldAvatarKey != null && !oldAvatarKey.isBlank()) {
      storageService.deleteFile(oldAvatarKey, StorageBucket.AVATARS);
    }

    membersRepository.updateAvatarUrl(memberId, avatarKey);
    return storageService.generatePresignedUrl(avatarKey, StorageBucket.AVATARS);
  }

  private Member resolveAvatarUrl(Member member) {
    if (member.getAvatarUrl() != null) {
      String presignedUrl = storageService.generatePresignedUrl(member.getAvatarUrl().toString(), StorageBucket.AVATARS);
      member.setAvatarUrl(URI.create(presignedUrl));
    }
    return member;
  }
}
