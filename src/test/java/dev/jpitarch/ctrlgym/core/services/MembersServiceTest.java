package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.DatePeriod;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberWithoutAccessException;
import dev.jpitarch.ctrlgym.core.dto.AccessTokensResponse;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.MembershipPlanRepository;
import dev.jpitarch.ctrlgym.payments.services.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembersServiceTest {

  @InjectMocks
  MembersService membersService;

  @Mock
  MembersRepository membersRepository;

  @Mock
  MembershipService membershipService;

  @Mock
  MembershipPlanRepository membershipPlanRepository;

  @Mock
  GenerateAccessQr generateAccessQr;

  @Mock
  CustomerService customerService;

  private final UUID memberId = UUID.randomUUID();

  @Test
  @DisplayName("Should generate tokens when membership plan is allDay and membership is active")
  void generateAccessTokens_allDay_returnsTokens() {
    Integer gymId = 1;
    String role = "MEMBER";
    Integer gymBranchId = 5;
    String planId = "plan-1";

    when(membersRepository.getGymIdByMemberId(memberId)).thenReturn(gymId);
    when(membersRepository.getRoleById(memberId)).thenReturn(role);
    when(membershipService.retrieve(memberId)).thenReturn(Optional.of(
      Membership.builder()
        .planId(planId)
        .datePeriod(DatePeriod.of(LocalDate.now().minusDays(30), LocalDate.now().plusDays(30)))
        .build()
    ));
    when(membershipPlanRepository.retrieve(planId)).thenReturn(
      MembershipPlan.builder()
        .id(planId)
        .allDay(true)
        .gymBranchId(gymBranchId)
        .build()
    );
    when(generateAccessQr.generateEntryToken(memberId, role, gymBranchId, gymBranchId)).thenReturn("entry-token");
    when(generateAccessQr.generateExitToken(memberId, role, gymId, gymBranchId)).thenReturn("exit-token");

    AccessTokensResponse result = membersService.generateAccessTokens(memberId);

    assertThat(result.getEntryToken()).isEqualTo("entry-token");
    assertThat(result.getExitToken()).isEqualTo("exit-token");
  }

  @Test
  @DisplayName("Should generate tokens when current time is within plan schedule")
  void generateAccessTokens_withinSchedule_returnsTokens() {
    Integer gymId = 1;
    String role = "MEMBER";
    Integer gymBranchId = 5;
    String planId = "plan-1";

    when(membersRepository.getGymIdByMemberId(memberId)).thenReturn(gymId);
    when(membersRepository.getRoleById(memberId)).thenReturn(role);
    when(membershipService.retrieve(memberId)).thenReturn(Optional.of(
      Membership.builder()
        .planId(planId)
        .datePeriod(DatePeriod.of(LocalDate.now().minusDays(30), LocalDate.now().plusDays(30)))
        .build()
    ));
    when(membershipPlanRepository.retrieve(planId)).thenReturn(
      MembershipPlan.builder()
        .id(planId)
        .allDay(false)
        .startTime(LocalTime.now().minusHours(2))
        .endTime(LocalTime.now().plusHours(2))
        .gymBranchId(gymBranchId)
        .build()
    );
    when(generateAccessQr.generateEntryToken(memberId, role, gymBranchId, gymBranchId)).thenReturn("entry-token");
    when(generateAccessQr.generateExitToken(memberId, role, gymId, gymBranchId)).thenReturn("exit-token");

    AccessTokensResponse result = membersService.generateAccessTokens(memberId);

    assertThat(result.getEntryToken()).isEqualTo("entry-token");
    assertThat(result.getExitToken()).isEqualTo("exit-token");
  }

  @Test
  @DisplayName("Should throw MemberWithoutAccessException when membership period is expired")
  void generateAccessTokens_expiredMembership_throwsException() {
    String planId = "plan-1";

    when(membersRepository.getGymIdByMemberId(memberId)).thenReturn(1);
    when(membersRepository.getRoleById(memberId)).thenReturn("MEMBER");
    when(membershipService.retrieve(memberId)).thenReturn(Optional.of(
      Membership.builder()
        .planId(planId)
        .datePeriod(DatePeriod.of(LocalDate.now().minusDays(60), LocalDate.now().minusDays(1)))
        .build()
    ));

    assertThatThrownBy(() -> membersService.generateAccessTokens(memberId))
      .isInstanceOf(MemberWithoutAccessException.class);
  }

  @Test
  @DisplayName("Should throw MemberWithoutAccessException when current time is before plan start time")
  void generateAccessTokens_beforeStartTime_throwsException() {
    Integer gymId = 1;
    String role = "MEMBER";
    String planId = "plan-1";

    when(membersRepository.getGymIdByMemberId(memberId)).thenReturn(gymId);
    when(membersRepository.getRoleById(memberId)).thenReturn(role);
    when(membershipService.retrieve(memberId)).thenReturn(Optional.of(
      Membership.builder()
        .planId(planId)
        .datePeriod(DatePeriod.of(LocalDate.now().minusDays(30), LocalDate.now().plusDays(30)))
        .build()
    ));
    when(membershipPlanRepository.retrieve(planId)).thenReturn(
      MembershipPlan.builder()
        .id(planId)
        .allDay(false)
        .startTime(LocalTime.now().plusHours(1))
        .endTime(LocalTime.now().plusHours(3))
        .gymBranchId(5)
        .build()
    );

    assertThatThrownBy(() -> membersService.generateAccessTokens(memberId))
      .isInstanceOf(MemberWithoutAccessException.class);
  }

  @Test
  @DisplayName("Should throw MemberWithoutAccessException when current time is after plan end time")
  void generateAccessTokens_afterEndTime_throwsException() {
    Integer gymId = 1;
    String role = "MEMBER";
    String planId = "plan-1";

    when(membersRepository.getGymIdByMemberId(memberId)).thenReturn(gymId);
    when(membersRepository.getRoleById(memberId)).thenReturn(role);
    when(membershipService.retrieve(memberId)).thenReturn(Optional.of(
      Membership.builder()
        .planId(planId)
        .datePeriod(DatePeriod.of(LocalDate.now().minusDays(30), LocalDate.now().plusDays(30)))
        .build()
    ));
    when(membershipPlanRepository.retrieve(planId)).thenReturn(
      MembershipPlan.builder()
        .id(planId)
        .allDay(false)
        .startTime(LocalTime.now().minusHours(3))
        .endTime(LocalTime.now().minusHours(1))
        .gymBranchId(5)
        .build()
    );

    assertThatThrownBy(() -> membersService.generateAccessTokens(memberId))
      .isInstanceOf(MemberWithoutAccessException.class);
  }
}
