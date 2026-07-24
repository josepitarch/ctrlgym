package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.MembershipMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface MembershipJpaRepository extends JpaRepository<MembershipMO, Long> {

  @Query("""
        SELECT m
        FROM MembershipMO m
        WHERE m.memberId = :memberId AND m.gymId = :gymId
        AND m.startDate <= CURRENT_DATE AND (m.endDate IS NULL OR m.endDate > CURRENT_DATE)
    """)
  Optional<MembershipMO> findByMemberIdAndGymId(UUID memberId, Integer gymId);

  Optional<MembershipMO> findByIdAndEndDateIsNull(Integer id);

  @Query("SELECT m.id FROM MembershipMO m WHERE m.stripeSubscriptionId = :stripeSubscriptionId")
  Long getIdByStripeSubscriptionId(String stripeSubscriptionId);

  @Query("SELECT m.stripeSubscriptionId FROM MembershipMO m WHERE m.memberId = :memberId AND m.gymId = :gymId")
  public String getStripeSubscriptionId(UUID memberId, Integer gymId);

  @Query("""
    SELECT COUNT(m) > 0
    FROM MembershipMO m
    WHERE m.memberId = :memberId AND m.gymId = :gymId
    AND m.membershipPlanId = :membershipPlanId
    AND m.startDate <= CURRENT_DATE AND (m.endDate IS NULL OR m.endDate >= CURRENT_DATE)
    """)
  boolean hasActiveMembership(UUID memberId, Integer gymId, String membershipPlanId);
}
