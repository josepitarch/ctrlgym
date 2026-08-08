package dev.jpitarch.ctrlgym.crons;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class PeakHourOccupancyCron {

  private final NamedParameterJdbcTemplate jdbc;

  @Scheduled(cron = "0 0 22 * * SUN", zone = "Europe/Madrid")
  public void calculatePeakHourOccupancy() {
    var sql = """
        WITH hourly_slots AS (
          SELECT
            gym_branch_id,
            DATE_TRUNC('hour', snapshot_time)::time AS slot_start,
            (DATE_TRUNC('hour', snapshot_time) + INTERVAL '1 hour')::time AS slot_end,
            SUM(count) AS total_count
          FROM gym_branch_occupancy_snapshot
          WHERE snapshot_time >= NOW() - INTERVAL '7 days'
          GROUP BY gym_branch_id, DATE_TRUNC('hour', snapshot_time)
        ),
        ranked AS (
          SELECT
            gym_branch_id,
            slot_start,
            slot_end,
            ROW_NUMBER() OVER (PARTITION BY gym_branch_id ORDER BY total_count DESC) AS rn
          FROM hourly_slots
        )
        SELECT gym_branch_id, slot_start, slot_end
        FROM ranked
        WHERE rn = 1
        """;

    List<Map<String, Object>> results = jdbc.queryForList(sql, Collections.emptyMap());

    var updateSql = """
        UPDATE gym_branches
        SET peak_hour_start = :peakStart,
            peak_hour_end = :peakEnd
        WHERE id = :gymBranchId
        """;

    var batchParams = results.stream().map(row -> {
      var ps = new MapSqlParameterSource();
      ps.addValue("gymBranchId", row.get("gym_branch_id"));
      ps.addValue("peakStart", LocalTime.parse(row.get("slot_start").toString()));
      ps.addValue("peakEnd", LocalTime.parse(row.get("slot_end").toString()));
      return ps;
    }).toArray(MapSqlParameterSource[]::new);

    jdbc.batchUpdate(updateSql, batchParams);
    log.info("Peak hour calculated for {} branches", results.size());
  }
}
