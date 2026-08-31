package dev.jpitarch.ctrlgym.core;

import dev.jpitarch.ctrlgym.core.repositories.GymJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
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

  private final UserJpaRepository userJpaRepository;

  public Optional<String> getStripeCustomerId(UUID memberId) {
    return userJpaRepository.getStripeCustomerId(memberId);
  }

  public Optional<String> getStripeSetupIntentId(UUID memberId) {
    return userJpaRepository.getStripeSetupIntentId(memberId);
  }

  public void saveStripeSetupIntentId(UUID memberId, String id) {
    userJpaRepository.saveStripeSetupIntentId(memberId, id);
  }

  public UUID getId(String stripeCustomerId) {
    var sql = """
        SELECT id
        FROM users
        WHERE stripe_customer_id = :stripeCustomerId
      """;

    var params = Map.of("stripeCustomerId", stripeCustomerId);

    return this.jdbc.queryForObject(sql, params, (rs, rowNum) -> UUID.fromString(rs.getString("id")));

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
