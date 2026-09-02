package dev.jpitarch.ctrlgym.core.services;

import com.stripe.exception.StripeException;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberNotFoundException;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberWithoutAccessException;
import dev.jpitarch.ctrlgym.core.dto.AccessTokensResponse;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipsRepository;
import dev.jpitarch.ctrlgym.payments.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
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

  private final MembershipsRepository membershipsRepository;

  private final GenerateAccessQr generateAccessQr;

  private final CustomerService customerService;

  @Transactional
  public void create(Member member) throws StripeException {
    if (!membersRepository.exists(member.getId())) {
      throw new MemberNotFoundException(member.getId());
    }

    String customerId = customerService.create(member);

    log.info("Setting member with id {} from {} to {}...", member.getId(), UserStatus.AUTH, member.getStatus());
    membersRepository.save(member, customerId);
  }

  public Member getMember(UUID memberId) {
    var member = membersRepository.getById(memberId);
    member.setIban(customerService.getIbanLast4(memberId).orElse(null));
    return member;
  }

  public AccessTokensResponse generateAccessTokens(UUID memberId) {
    Integer gymId = membersRepository.getGymIdByMemberId(memberId);
    List<Integer> branches = membershipsRepository.getAccessibleBranches(memberId, gymId);
    String role = membersRepository.getRoleById(memberId);

    if (CollectionUtils.isEmpty(branches)) throw new MemberWithoutAccessException(memberId);

    log.info("Generating access tokens for member with id {}: {}...", memberId, branches);

    String entryToken = generateAccessQr.generateEntryToken(memberId, role, gymId, branches);
    String exitToken = generateAccessQr.generateExitToken(memberId, role, gymId, branches);

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
