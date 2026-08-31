package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipJpaRepository extends JpaRepository<MembershipEntity, Long> {

  @Query("""
        SELECT m
        FROM MembershipEntity m
        WHERE m.memberId = :memberId
    """)
  List<MembershipEntity> findByMemberId(UUID memberId);

  Optional<MembershipEntity> findByIdAndEndDateIsNull(Long id);

  @Query("SELECT m.id FROM MembershipEntity m WHERE m.stripeSubscriptionId = :stripeSubscriptionId")
  Long getIdByStripeSubscriptionId(String stripeSubscriptionId);

  @Query("SELECT m.stripeSubscriptionId FROM MembershipEntity m WHERE m.memberId = :memberId")
  public String getStripeSubscriptionId(UUID memberId);

  @Query("""
    SELECT COUNT(m) > 0
    FROM MembershipEntity m
    WHERE m.memberId = :memberId AND m.membershipPlanId = :membershipId
    AND m.startDate <= CURRENT_DATE AND (m.endDate IS NULL OR m.endDate >= CURRENT_DATE)
    """)
  boolean hasActiveMembership(UUID memberId, String membershipId);
}
