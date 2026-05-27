package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.DatePeriod;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.enums.Granularity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class GymBranchOccupancyRepository {

  private final NamedParameterJdbcTemplate jdbc;

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
