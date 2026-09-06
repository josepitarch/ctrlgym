package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.entities.MembershipEntity;
import dev.jpitarch.ctrlgym.core.mappers.MembershipMapper;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipCancellationReasonJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MembershipsRepository {

  private final MembershipJpaRepository membershipJpaRepository;

  private final MembershipCancellationReasonJpaRepository cancellationReasonJpaRepository;

  private final MembershipMapper mapper;

  public Membership save(UUID memberId, Integer gymId, String membershipPlanId, String subscriptionId, LocalDate nextBillingDate) {
    var membership = new MembershipEntity();
    membership.setMemberId(memberId);
    membership.setGymId(gymId);
    membership.setMembershipPlanId(membershipPlanId);
    membership.setStartDate(LocalDate.now());
    membership.setStripeSubscriptionId(subscriptionId);
    membership.setAutoRenew(Boolean.TRUE);
    membership.setNextBillingDate(nextBillingDate);

    return mapper.map(membershipJpaRepository.save(membership));
  }

  public Long getIdByStripeSubscriptionId(String subscriptionId) {
    return this.membershipJpaRepository.getIdByStripeSubscriptionId(subscriptionId);
  }

  public List<Membership> getMemberships(UUID memberId) {
    return membershipJpaRepository
      .findByMemberId(memberId)
      .stream()
      .map(mapper::map)
      .toList();
  }

  public void updateNextBillingDate(Long membershipId, LocalDate nextBillingDate) {
    membershipJpaRepository.findById(membershipId).ifPresent(m -> m.setNextBillingDate(nextBillingDate));
  }

  public void setCancellationReasonId(Long membershipId, LocalDate endDate, Integer cancellationReasonId, String comment) {
    membershipJpaRepository
      .findByIdAndEndDateIsNull(membershipId)
      .ifPresent(m -> {
        m.setEndDate(endDate);
        m.setCancellationReasonId(cancellationReasonId);
        m.setCancellationComment(comment);
        membershipJpaRepository.save(m);
      });
  }

  public void setMembershipPlanId(Long id, String membershipPlanId) {
    this.membershipJpaRepository.findById(id)
      .ifPresent(m -> {
        m.setMembershipPlanId(membershipPlanId);
        membershipJpaRepository.save(m);
      });
  }

  public boolean hasActiveMembership(UUID memberId, String membershipId) {
    return membershipJpaRepository.hasActiveMembership(memberId, membershipId);
  }


  public List<MembershipCancellationReason> getCancellationReasons(String language) {
    return cancellationReasonJpaRepository.findByLanguageCode(language)
      .stream()
      .map(mapper::toDomain)
      .toList();
  }

}
