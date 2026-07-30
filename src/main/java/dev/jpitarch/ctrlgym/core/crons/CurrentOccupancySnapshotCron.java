package dev.jpitarch.ctrlgym.core.crons;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentOccupancySnapshotCron {

  private final NamedParameterJdbcTemplate jdbc;

  @Scheduled(cron = "0 0/15 * * * *")
  public void snapshotCurrentOccupancy() {
    var sql = """
        INSERT INTO gym_branch_occupancy_snapshot (gym_branch_id, snapshot_time, count)
        SELECT gym_branch_id, NOW(), count
        FROM gym_branch_current_occupancy
        """;

    var rows = jdbc.update(sql, Collections.emptyMap());
    log.info("Occupancy snapshot taken for {} branches", rows);
  }
}
