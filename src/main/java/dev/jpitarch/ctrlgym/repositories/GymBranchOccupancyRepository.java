package dev.jpitarch.ctrlgym.repositories;

import dev.jpitarch.ctrlgym.domain.GymBranchId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GymBranchOccupancyRepository {

  private final NamedParameterJdbcTemplate jdbc;

  public List<String[]> getDailyOccupancies(GymBranchId gymBranchId) {
    var sql = """
              SELECT
              EXTRACT(HOUR FROM snapshot_time) AS hour,
              AVG(count) AS avg_occupancy
              FROM gym_branch_occupancy_snapshot
              WHERE gym_branch_id = 1000
                AND snapshot_time >= '2026-05-14'::date
                AND snapshot_time < '2026-05-14'::date + INTERVAL '1 day'
              GROUP BY 1
              ORDER BY 1;
            """;

    return jdbc.query(sql, (row, _) -> {
      var hour = row.getInt("hour");
      var avgOccupancy = row.getInt("avg_occupancy");
      return new String[]{ String.valueOf(hour), String.valueOf(avgOccupancy) };
    });
  }

  public List<String[]> getWeeklyOccupancies(GymBranchId gymBranchId) {
    var sql = """
            SELECT
            EXTRACT(ISODOW FROM snapshot_time) AS weekday,
            AVG(count) AS avg_occupancy
            FROM gym_branch_occupancy_snapshot
            WHERE gym_branch_id = 1000
              AND snapshot_time >= '2026-05-11'::date
              AND snapshot_time < '2026-05-11'::date + INTERVAL '7 day'
            GROUP BY 1
            ORDER BY 1;
            """;
    return jdbc.query(sql, (row, _) -> {
      var weekday = row.getInt("weekday");
      var avgOccupancy = row.getInt("avg_occupancy");
      return new String[]{ String.valueOf(weekday), String.valueOf(avgOccupancy) };
    });
  }

  public List<String[]> getMonthlyOccupancies(GymBranchId gymBranchId) {
   var sql = """
           SELECT
               EXTRACT(WEEK FROM snapshot_time)
                   - EXTRACT(WEEK FROM DATE_TRUNC('month', snapshot_time))
                   + 1 AS week_of_month,
               AVG(count) AS avg_occupancy
           FROM gym_branch_occupancy_snapshot
           WHERE gym_branch_id = 1000
             AND snapshot_time >= DATE_TRUNC('month', '2026-04-01'::date)
             AND snapshot_time < DATE_TRUNC('month', '2026-04-30'::date) + INTERVAL '1 month'
           GROUP BY 1
           ORDER BY 1;
           """;

    return jdbc.query(sql, (row, _) -> {
      var weekOfMonth = row.getInt("week_of_month");
      var avgOccupancy = row.getInt("avg_occupancy");
      return new String[]{ String.valueOf(weekOfMonth), String.valueOf(avgOccupancy) };
    });
  }
}
