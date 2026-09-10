package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberWithoutAccessException;
import dev.jpitarch.ctrlgym.core.dto.AccessTokensResponse;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipPlanRepository;
import dev.jpitarch.ctrlgym.payments.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembersService {

  private final MembersRepository membersRepository;

  private final GenerateAccessQr generateAccessQr;

  private final CustomerService customerService;

  private final MembershipService membershipService;

  private final MembershipPlanRepository membershipPlanRepository;

  public Member getMember(UUID memberId) {
    var member = membersRepository.getById(memberId);
    member.setIban(customerService.getIbanLast4(memberId).orElse(null));
    return member;
  }

  public AccessTokensResponse generateAccessTokens(UUID memberId) {
    Integer gymId = membersRepository.getGymIdByMemberId(memberId);
    String role = membersRepository.getRoleById(memberId);
    Membership membership = membershipService.retrieve(memberId)
      .orElseThrow(() -> new MemberWithoutAccessException(memberId));

    if (membership.getDateRange().isPast()) {
      throw new MemberWithoutAccessException(memberId);
    }

    MembershipPlan plan = membershipPlanRepository.retrieve(membership.getPlanId());

    if (!plan.isAllDay()) {
      var now = LocalTime.now();
      var startTime = plan.getStartTime();
      var endTime = plan.getEndTime();

      boolean isWithinSchedule;
      if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
        isWithinSchedule = !now.isBefore(startTime) && !now.isAfter(endTime);
      } else {
        isWithinSchedule = !now.isBefore(startTime) || !now.isAfter(endTime);
      }

      if (!isWithinSchedule) {
        throw new MemberWithoutAccessException(memberId);
      }
    }

    log.info("Generating access tokens for member with id {}: {}...", memberId, plan.getGymBranchId());

    String entryToken = generateAccessQr.generateEntryToken(memberId, role, plan.getGymBranchId(), plan.getGymBranchId());
    String exitToken = generateAccessQr.generateExitToken(memberId, role, gymId, plan.getGymBranchId());

    return new AccessTokensResponse(entryToken, exitToken);
  }

  public List<MemberAccess> getAccesses(UUID memberId) {
    return membersRepository.getMemberAccessesByMemberId(memberId);
  }

  public Map<LocalDate, Boolean> getAttendanceSummary(UUID memberId, LocalDate from, LocalDate to) {
    OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
    OffsetDateTime toDt = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

    var accesses = membersRepository.getMemberAccessesByMemberIdAndDateRange(memberId, fromDt, toDt);

    var accessedDates = accesses.stream()
      .map(access -> access.getTimestamp().toLocalDate())
      .collect(Collectors.toSet());

    return from.datesUntil(to.plusDays(1))
      .collect(Collectors.toMap(
        date -> date,
        accessedDates::contains
      ));
  }


}
