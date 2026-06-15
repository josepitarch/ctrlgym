package dev.jpitarch.ctrlgym.core.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class GymsRepository {

  private final NamedParameterJdbcTemplate jdbc;

  public String getApiKey(Integer gymId) {
    var sql = """
        SELECT verifacti_api_key
        FROM gyms
        WHERE id = :gymId
      """;

    var params = Map.of("gymId", gymId);

    return this.jdbc.queryForObject(sql, params, String.class);
  }

  public String getStripeAccountId(Integer gymId) {
    var sql = """
      SELECT stripe_account_id
        FROM gyms
        WHERE id = :gymId
      """;
    var params = Map.of("gymId", gymId);
    return this.jdbc.queryForObject(sql, params, String.class);


  }

  public Integer getId(String stripeAccountId) {
    var sql = """
        SELECT id
        FROM gyms
        WHERE stripe_account_id = :stripeAccountId
      """;

    var params = Map.of("stripeAccountId", stripeAccountId);

    return this.jdbc.queryForObject(sql, params, Integer.class);
  }

}
