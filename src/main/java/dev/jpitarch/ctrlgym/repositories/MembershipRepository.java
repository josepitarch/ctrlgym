package dev.jpitarch.ctrlgym.repositories;

import dev.jpitarch.ctrlgym.domain.DatePeriod;
import dev.jpitarch.ctrlgym.domain.GymBranchId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MembershipRepository {

  private final NamedParameterJdbcTemplate jdbc;

  public List<String[]> getCurrentCount(GymBranchId gymBranchId, DatePeriod datePeriod) {
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

  public List<String[]> getNewsCount(GymBranchId gymBranchId, DatePeriod datePeriod) {
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

  public List<String[]> getCancelledCount(GymBranchId gymBranchId, DatePeriod datePeriod) {
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

  public Integer getSeniorityAverage(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      SELECT
      AVG(CURRENT_DATE - start_date) AS avg_days
      FROM membership
      WHERE gym_id = :gymId
        AND start_date <= CURRENT_DATE
        AND (end_date IS NULL OR end_date >= CURRENT_DATE);
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId()
    );

    return jdbc.query(sql, params, (row, _) -> row.getInt("avg_days")).stream().findFirst().orElse(0);
  }

  public List<String[]> getCohorts(GymBranchId gymBranchId) {
    var sql = """
      WITH cohorts AS (
          SELECT
              id,
              DATE_TRUNC('month', start_date)::date AS cohort_month,
              start_date,
              end_date
          FROM membership
          WHERE gym_id = :gymId
      ),
      
      cohort_activity AS (
          SELECT
              c.cohort_month,
      
              gs.month_offset,
      
              COUNT(*) FILTER (
                  WHERE c.end_date IS NULL
                     OR c.end_date >= c.cohort_month
                          + (gs.month_offset || ' month')::interval
              ) AS active_members
      
          FROM cohorts c
      
          CROSS JOIN generate_series(0, :currentMonth) AS gs(month_offset)
      
          GROUP BY 1, 2
      ),
      
      cohort_sizes AS (
          SELECT
              cohort_month,
              COUNT(*) AS cohort_size
          FROM cohorts
          GROUP BY 1
      )
      
      SELECT
          ca.cohort_month,
          ca.month_offset,
      
          ca.active_members,
      
          cs.cohort_size,
      
          ROUND(
              ca.active_members * 100.0 / cs.cohort_size,
              2
          ) AS retention_rate
      
      FROM cohort_activity ca
      JOIN cohort_sizes cs
          ON cs.cohort_month = ca.cohort_month
      
      ORDER BY 1, 2;
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId(),
      "currentMonth", LocalDate.now().getMonthValue()
    );
    return jdbc.query(sql, params, (row, _) -> {
      var cohortMonth = row.getDate("cohort_month").toLocalDate();
      var monthOffset = row.getInt("month_offset");
      var activeMembers = row.getInt("active_members");
      var cohortSize = row.getInt("cohort_size");
      var retentionRate = row.getDouble("retention_rate");
      return new String[]{ cohortMonth.toString(), String.valueOf(monthOffset), String.valueOf(activeMembers), String.valueOf(cohortSize), String.valueOf(retentionRate) };
    });

  }

}
