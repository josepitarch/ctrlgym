package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.DatePeriod;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Membership;
import dev.jpitarch.ctrlgym.core.domain.MembershipCancellationReason;
import dev.jpitarch.ctrlgym.core.models.MembershipCancellationReasonTranslationMO;
import dev.jpitarch.ctrlgym.core.models.MembershipMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipCancellationReasonJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MembershipsRepository {

  private final MembershipJpaRepository membershipJpaRepository;

  private final MembershipPlanRepository membershipPlanRepository;

  private final MembershipCancellationReasonJpaRepository cancellationReasonJpaRepository;

  private final NamedParameterJdbcTemplate jdbc;

  public void save(Member.Id memberId, String membershipPlanId, String subscriptionId, LocalDate nextBillingDate) {
    var membership = new MembershipMO();
    membership.setMemberId(memberId.memberId());
    membership.setGymId(memberId.gymId());
    membership.setMembershipPlanId(membershipPlanId);
    membership.setStartDate(LocalDate.now());
    membership.setStripeSubscriptionId(subscriptionId);
    membership.setAutoRenew(true);
    membership.setNextBillingDate(nextBillingDate);

    membershipJpaRepository.save(membership);
  }

  private Optional<MembershipMO> getMembership(Integer id) {
    return this.membershipJpaRepository.findById(id.longValue());
  }

  public Optional<Membership> getMembership(Member.Id memberId) {
    return membershipJpaRepository
            .findByMemberIdAndGymId(memberId.memberId(), memberId.gymId())
            .map(this::map);
  }

  public void setCancellationReasonId(Integer membershipId, Integer cancellationReasonId, String comment) {
    membershipJpaRepository
            .findByIdAndEndDateIsNull(membershipId)
            .ifPresent(m -> {
              m.setEndDate(LocalDate.now());
              m.setCancellationReasonId(cancellationReasonId);
              m.setCancellationComment(comment);
              membershipJpaRepository.save(m);
            });
  }

  public boolean hasActiveMembership(Member.Id memberId, String membershipPlanId) {
    return membershipJpaRepository.hasActiveMembership(memberId.memberId(), memberId.gymId(), membershipPlanId);
  }

  public List<Integer> getAccessibleBranches(Member.Id memberId) {
    //TODO: aquí habría que comprobar que si all_branches está activado
    // devolver todos los centros en gym_branches
    var sql = """
            SELECT mp.gym_branch_id
            FROM memberships m
            INNER JOIN membership_plans mp ON m.membership_plan_id = mp.id
            WHERE m.member_id = :memberId AND mp.gym_id = :gymId
            AND m.start_date <= CURRENT_DATE AND (m.end_date IS NULL OR m.end_date >= CURRENT_DATE)
            """;

    var params = Map.of(
            "memberId", memberId.memberId(),
            "gymId", memberId.gymId()
    );

    return jdbc.queryForList(sql, params, Integer.class);
  }

  public List<MembershipCancellationReason> getCancellationReasons(String language) {
    return cancellationReasonJpaRepository.findByLanguageCode(language)
            .stream()
            .map(this::toDomain)
            .toList();
  }


  private MembershipCancellationReason toDomain(MembershipCancellationReasonTranslationMO translation) {
    return MembershipCancellationReason.builder()
            .id(translation.getCancellationReason().getId())
            .name(translation.getName())
            .description(translation.getDescription())
            .build();
  }


  private Membership map(MembershipMO m) {
    return Membership.builder()
            .id(m.getId().intValue())
            .recurring(Membership.Recurring.from("MONTHLY")) //TODO
            .datePeriod(new DatePeriod(m.getStartDate(), m.getEndDate()))
            .nextBillingDate(m.getNextBillingDate())
            .build();
  }
}
