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

  public Optional<String> getStripeSetupIntentId(Member.Id id) {
    return memberJpaRepository.getStripeSetupIntentId(id.memberId(), id.gymId());
  }

  public Optional<String> getStripeSetupIntentId(String stripeCustomerId) {
    return memberJpaRepository.getStripeSetupIntentId(stripeCustomerId);
  }

  public void saveStripeSetupIntentId(Member.Id memberId, String id) {
    memberJpaRepository.saveStripeSetupIntentId(memberId.memberId(), memberId.gymId(), id);
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

  public String getStripeSubscriptionId(Long membershipId) {
    var sql = """
        SELECT stripe_subscription_id
        FROM memberships
        WHERE id = :id
      """;

    var params = Map.of(
      "id", membershipId
    );

    return jdbc.queryForObject(sql, params, String.class);
  }

  public Long getMembershipId(String stripeSubscriptionId) {
    var sql = """
        SELECT id
        FROM memberships
        WHERE stripe_subscription_id = :stripeSubscriptionId
      """;

    var params = Map.of(
      "stripeSubscriptionId", stripeSubscriptionId
    );

    return jdbc.queryForObject(sql, params, Long.class);
  }


}
