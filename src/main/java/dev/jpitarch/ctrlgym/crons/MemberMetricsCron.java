package dev.jpitarch.ctrlgym.crons;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class MemberMetricsCron {

  private final NamedParameterJdbcTemplate jdbc;

  @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Madrid")
  public void calculateMonthlyMetrics() {
    var currentMonth = YearMonth.now();
    var yearMonth = currentMonth.atDay(1);
    var isLastDayOfMonth = LocalDate.now().equals(currentMonth.atEndOfMonth());

    var sql = """
        SELECT
          u.id AS member_id,
          COALESCE(
            (SELECT COUNT(*)
             FROM member_accesses ma
             WHERE ma.member_id = u.id
               AND ma.direction = 0
               AND ma.created_at >= :yearMonth::date
               AND ma.created_at < (:yearMonth::date + INTERVAL '1 month')::date
            ), 0) AS attendance
        FROM users u
        WHERE u.status = 'ACTIVE'
       """;

    var params = Map.of("yearMonth", yearMonth.toString());

    List<Map<String, Object>> results = jdbc.queryForList(sql, params);

    var upsertSql = """
        INSERT INTO member_metrics_monthly
          (member_id, year_month, attendance, is_closed, calculated_at)
        VALUES
          (:memberId, :yearMonth, :attendance, :isClosed, NOW())
        ON CONFLICT (member_id, year_month) DO UPDATE SET
          attendance = EXCLUDED.attendance,
          is_closed = EXCLUDED.is_closed,
          calculated_at = NOW()
      """;

    var batchParams = results.stream().map(row -> {
      var ps = new MapSqlParameterSource();
      ps.addValue("memberId", row.get("member_id"));
      ps.addValue("yearMonth", yearMonth);
      ps.addValue("attendance", ((Number) row.get("attendance")).intValue());
      ps.addValue("isClosed", isLastDayOfMonth);
      return ps;
    }).toArray(MapSqlParameterSource[]::new);

    jdbc.batchUpdate(upsertSql, batchParams);
    log.info("Member metrics calculated for {} members, month={}", results.size(), currentMonth);
  }
}
