package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StripeBridge {

  private final NamedParameterJdbcTemplate jdbc;

  private final GymJpaRepository gymRepository;

  private final MemberJpaRepository memberJpaRepository;

  public Optional<String> getStripeCustomerId(Member.Id memberId) {
    return memberJpaRepository.getStripeCustomerId(memberId.memberId(), memberId.gymId());
  }

  public Optional<String> getPaymentMethodId(Member.Id id) {
    return memberJpaRepository.getStripePaymentMethodId(id.memberId(), id.gymId());
  }

  public Optional<String> getPaymentMethodId(String stripeCustomerId) {
    return memberJpaRepository.getStripePaymentMethodId(stripeCustomerId);
  }

  public Member.Id getId(String stripeCustomerId) {
    var sql = """
        SELECT id, gym_id
        FROM users
        WHERE stripe_customer_id = :stripeCustomerId
      """;

    var params = Map.of("stripeCustomerId", stripeCustomerId);

    return this.jdbc.queryForObject(sql, params, (rs, _) -> Member.Id.of(
      UUID.fromString(rs.getString("id")),
      rs.getInt("gym_id"))
    );

  }

  public String getStripePriceId(String id) {
    var sql = """
      SELECT stripe_price_id
      FROM membership_plans
      WHERE id = :id
      """;
    var params = Map.of("id", id);

    return jdbc.queryForObject(sql, params, String.class);
  }

  public String getStripeAccountId(Integer gymId) {
    return gymRepository.findStripeAccountIdById(gymId);
  }

  public String getStripeSubscriptionId(Member.Id memberId, Integer membershipId) {
    var sql = """
        SELECT stripe_subscription_id
        FROM memberships
        WHERE member_id = :memberId AND gym_id = :gymId AND id = :membershipId
      """;

    var params = Map.of(
      "memberId", memberId.memberId(),
      "gymId", memberId.gymId(),
      "id", membershipId
    );

    return jdbc.queryForObject(sql, params, String.class);
  }


}
