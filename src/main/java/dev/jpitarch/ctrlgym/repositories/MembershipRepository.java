package dev.jpitarch.ctrlgym.repositories;

import dev.jpitarch.ctrlgym.domain.DatePeriod;
import dev.jpitarch.ctrlgym.domain.GymBranchId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MembershipRepository {

  private final NamedParameterJdbcTemplate jdbc;

  public List<String[]> getMembershipsCounter(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
            SELECT
                month::date,
                COUNT(m.id) AS active_memberships
            FROM generate_series(
                :from::date,
                :to::date,
                INTERVAL '1 month'
            ) month
            LEFT JOIN membership m
                ON m.gym_id = :gymId
               AND m.start_date <= month
               AND (m.end_date IS NULL OR m.end_date >= month)
            GROUP BY 1
            ORDER BY 1;
            """;

    var params = Map.of(
            "gymId", gymBranchId.gymId(),
            "from", datePeriod.from().toString(),
            "to", datePeriod.to().toString()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = row.getDate("month").toLocalDate();
      var activeMemberships = row.getInt("active_memberships");
      return new String[]{ month.toString(), String.valueOf(activeMemberships) };
    });

  }

  public List<String[]> getNewsMembershipsCounter(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
            SELECT
                DATE_TRUNC('month', start_date)::date AS month,
                COUNT(*) AS new_memberships
            FROM membership
            WHERE gym_id = :gymId
            AND start_date >= :from AND start_date <= :to
            GROUP BY 1
            ORDER BY 1;
            """;

    var params = Map.of(
            "gymId", gymBranchId.gymId(),
            "from", datePeriod.from(),
            "to", datePeriod.to()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = row.getDate("month").toLocalDate();
      var newMemberships = row.getInt("new_memberships");
      return new String[]{ month.toString(), String.valueOf(newMemberships) };
    });
  }

  public List<String[]> getFinishedMembershipsCounter(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
            SELECT
                DATE_TRUNC('month', end_date)::date AS month,
                COUNT(*) AS cancellations
            FROM membership
            WHERE gym_id = :gymId
            AND start_date >= :from AND start_date <= :to
            AND end_date IS NOT NULL AND end_date <= CURRENT_DATE
            GROUP BY 1
            ORDER BY 1;
            """;

    var params = Map.of(
            "gymId", gymBranchId.gymId(),
            "from", datePeriod.from(),
            "to", datePeriod.to()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = row.getDate("month").toLocalDate();
      var cancellations = row.getInt("cancellations");
      return new String[]{ month.toString(), String.valueOf(cancellations) };
    });

  }
}
