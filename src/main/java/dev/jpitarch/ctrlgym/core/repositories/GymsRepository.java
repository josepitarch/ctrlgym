package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.DatePeriod;
import dev.jpitarch.ctrlgym.core.domain.GymBranch;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.enums.Granularity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
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

  public GymBranch getGymBranch(GymBranchId gymBranchId) {
    var sql = """
        SELECT b.id, b.name, b.capacity, b.peak_hour_start, b.peak_hour_end
        FROM gym_branches b
        WHERE b.gym_id = :gymId AND b.id = :branchId
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId(),
      "branchId", gymBranchId.branchId()
    );

    return this.jdbc.queryForObject(sql, params, (rs, rowNum) ->
      GymBranch.builder()
        .id(rs.getShort("id"))
        .name(rs.getString("name"))
        .capacity(rs.getShort("capacity"))
        .peakHour(new GymBranch.PeakHour(
          rs.getTime("peak_hour_start").toLocalTime(),
          rs.getTime("peak_hour_end").toLocalTime()
        ))
        .build()
    );
  }

  public Short getCurrentOccupancy(GymBranchId gymBranchId) {
    var sql = """
          SELECT count
          FROM gym_branch_current_occupancy
          WHERE gym_branch_id = :gymBranchId
      """;

    var params = Map.of("gymBranchId", gymBranchId.branchId());

    return this.jdbc.queryForObject(sql, params, Short.class);
  }

  public List<Map<String, Integer>> getOccupancies(GymBranchId gymBranchId, DatePeriod datePeriod, Granularity granularity) {
    var sql = """
      SELECT
      DATE_TRUNC(:granularity, snapshot_time) AS bucket,
      AVG(count) AS avg_occupancy
      FROM gym_branch_occupancy_snapshot
      WHERE gym_branch_id = :gymBranchId
      AND snapshot_time BETWEEN :from AND :to
      GROUP BY 1
      ORDER BY 1;
      """;

    var params = Map.of(
      "gymBranchId", gymBranchId.branchId(),
      "from", datePeriod.from(),
      "to", datePeriod.to(),
      "granularity", convertGranularity(granularity)
    );

    return jdbc.query(sql, params, (row, _) -> {
      var bucket = row.getTimestamp("bucket").toLocalDateTime();
      var avgOccupancy = row.getInt("avg_occupancy");
      return Map.of(bucket.toString(), avgOccupancy);
    });
  }

  private String convertGranularity(Granularity granularity) {
    return switch (granularity) {
      case HOURS -> "hour";
      case DAILY -> "day";
      case WEEKLY -> "week";
      case MONTHLY -> "month";
    };
  }

}
