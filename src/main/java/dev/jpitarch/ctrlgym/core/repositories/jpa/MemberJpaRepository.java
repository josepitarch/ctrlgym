package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.MemberMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberJpaRepository extends JpaRepository<MemberMO, MemberMO.ID> {

  @Query("""
        SELECT m.stripeCustomerId
        FROM MemberMO m
        WHERE m.id = :memberId AND m.gymId = :gymId
    """)
  Optional<String> getStripeCustomerId(UUID memberId, Integer gymId);

  @Query("""
        SELECT m.stripePaymentMethodId
        FROM MemberMO m
        WHERE m.id = :memberId AND m.gymId = :gymId
    """)
  Optional<String> getStripePaymentMethodId(UUID memberId, Integer gymId);

  @Query("""
        SELECT m.stripePaymentMethodId
        FROM MemberMO m
        WHERE m.stripeCustomerId = :stripeCustomerId
    """)
  Optional<String> getStripePaymentMethodId(String stripeCustomerId);
}
