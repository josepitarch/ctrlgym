package dev.jpitarch.ctrlgym.core.usecases;

import com.google.zxing.WriterException;
import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.services.MembersService;
import dev.jpitarch.ctrlgym.core.services.MembershipService;
import dev.jpitarch.ctrlgym.core.services.RoutinesService;
import dev.jpitarch.ctrlgym.core.services.WorkoutsService;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberUseCase {

  private final MembersService membersService;

  private final MembershipService membershipService;

  private final WorkoutsService workoutsService;

  private final RoutinesService routinesService;

  public void createMember(Member member) throws StripeException {
    membersService.create(member);
  }

  public Member getMember(Member.Id memberId) {
    return membersService.getMember(memberId);
  }

  public void initializeMembership(Member.Id memberId, String membershipId) throws StripeException {
    membershipService.initialize(memberId, membershipId);
  }

  @SneakyThrows
  public void changeMembership(Member.Id memberId, String newMembershipPlanId) {
    membershipService.change(memberId, newMembershipPlanId);
  }

  public void cancelMembership(Member.Id memberId, Integer membershipId, Integer cancellationReasonId, String comment) throws StripeException {
    membershipService.cancel(memberId, membershipId, cancellationReasonId, comment);
  }

  public Membership getMembership(Member.Id memberId) {
    return membershipService.retrieve(memberId);
  }

  public List<MemberAccess> getAccesses(Member.Id memberId) {
    return membersService.getAccesses(memberId);
  }

  public Map<LocalDate, Boolean> getAttendanceSummary(Member.Id memberId, LocalDate from, LocalDate to) {
    return membersService.getAttendanceSummary(memberId, from, to);
  }

  public void createRoutine(Routine routine, Member.Id memberId) {
    routinesService.create(routine, memberId);
  }

  public List<Routine> getRoutines(Member.Id memberId) {
    return routinesService.getRoutines(memberId);
  }

  public void deleteRoutine(Integer routineId, Member.Id memberId) {
    routinesService.delete(routineId, memberId);
  }

  public void createWorkout(Workout workout, UUID memberId) {
    workoutsService.create(workout, memberId);
  }

  public Page<Workout> getWorkouts(UUID memberId, Pageable pageable) {
    return workoutsService.getWorkouts(memberId, pageable);
  }

  public byte[] generateQrCode(Member.Id memberId) throws WriterException, IOException {
    return membersService.generateQrCode(memberId);
  }

}
