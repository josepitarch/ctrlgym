package dev.jpitarch.ctrlgym.core.crons;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class GymMetricsCron {

  private final NamedParameterJdbcTemplate jdbc;

  @Scheduled(cron = "0 0 23 * * *")
  public void calculateMonthlyMetrics() {
    var currentMonth = YearMonth.now();
    var yearMonth = currentMonth.atDay(1);
    var isLastDayOfMonth = LocalDate.now().equals(currentMonth.atEndOfMonth());

    var sql = """
        WITH month_period AS (
          SELECT
            :yearMonth::date AS month_start,
            (:yearMonth::date + INTERVAL '1 month' - INTERVAL '1 day')::date AS month_end
        ),
        branch_metrics AS (
          SELECT
            gb.id AS gym_branch_id,
            gb.gym_id,
            gb.is_active,
            COALESCE(active.cnt, 0) AS active_members,
            COALESCE(new_m.cnt, 0) AS new_members,
            COALESCE(churned.cnt, 0) AS churned_members
          FROM gym_branches gb
          WHERE gb.is_active = true
          LEFT JOIN LATERAL (
            SELECT COUNT(*) AS cnt
            FROM memberships m
            JOIN membership_plans mp ON m.membership_plan_id = mp.id
            WHERE mp.gym_branch_id = gb.id
              AND m.gym_id = gb.gym_id
              AND m.start_date <= (SELECT month_end FROM month_period)
              AND (m.end_date IS NULL OR m.end_date >= (SELECT month_start FROM month_period))
          ) active ON true
          LEFT JOIN LATERAL (
            SELECT COUNT(*) AS cnt
            FROM memberships m
            JOIN membership_plans mp ON m.membership_plan_id = mp.id
            WHERE mp.gym_branch_id = gb.id
              AND m.gym_id = gb.gym_id
              AND m.start_date >= (SELECT month_start FROM month_period)
              AND m.start_date <= (SELECT month_end FROM month_period)
          ) new_m ON true
          LEFT JOIN LATERAL (
            SELECT COUNT(*) AS cnt
            FROM memberships m
            JOIN membership_plans mp ON m.membership_plan_id = mp.id
            WHERE mp.gym_branch_id = gb.id
              AND m.gym_id = gb.gym_id
              AND m.end_date IS NOT NULL
              AND m.end_date >= (SELECT month_start FROM month_period)
              AND m.end_date <= (SELECT month_end FROM month_period)
          ) churned ON true
          LEFT JOIN LATERAL (
            SELECT COALESCE(SUM(i.total), 0) AS revenue
            FROM invoices i
            WHERE i.gym_id = gb.gym_id
              AND i.issue_at >= (SELECT month_start FROM month_period)
              AND i.issue_at <= (SELECT month_end FROM month_period)
              AND EXISTS (
                SELECT 1
                FROM memberships m
                JOIN membership_plans mp ON m.membership_plan_id = mp.id
                WHERE m.member_id = i.member_id
                  AND m.gym_id = i.gym_id
                  AND mp.gym_branch_id = gb.id
              )
          ) revenue_calc ON true
        )
        SELECT
          gym_branch_id,
          active_members,
          new_members,
          churned_members,
          is_active,
          COALESCE(revenue_calc.revenue, 0) AS revenue,
          CASE WHEN active_members > 0
            THEN ROUND(churned_members::numeric / (active_members + new_members) * 100, 2)
            ELSE 0
          END AS churn_rate
        FROM branch_metrics
      """;

    var params = Map.of("yearMonth", yearMonth.toString());

    List<Map<String, Object>> results = jdbc.queryForList(sql, params);

    var upsertSql = """
        INSERT INTO gym_metrics_monthly
          (gym_branch_id, year_month, active_members, new_members, churned_members, churn_rate, revenue, is_closed, calculated_at)
        VALUES
          (:gymBranchId, :yearMonth, :activeMembers, :newMembers, :churnedMembers, :churnRate, :revenue, :isClosed, NOW())
        ON CONFLICT (gym_branch_id, year_month) DO UPDATE SET
          active_members = EXCLUDED.active_members,
          new_members = EXCLUDED.new_members,
          churned_members = EXCLUDED.churned_members,
          churn_rate = EXCLUDED.churn_rate,
          revenue = EXCLUDED.revenue,
          is_closed = EXCLUDED.is_closed,
          calculated_at = NOW()
      """;

    var batchParams = results.stream().map(row -> {
      var ps = new MapSqlParameterSource();
      ps.addValue("gymBranchId", row.get("gym_branch_id"));
      ps.addValue("yearMonth", yearMonth);
      ps.addValue("activeMembers", ((Number) row.get("active_members")).intValue());
      ps.addValue("newMembers", ((Number) row.get("new_members")).intValue());
      ps.addValue("churnedMembers", ((Number) row.get("churned_members")).intValue());
      ps.addValue("churnRate", row.get("churn_rate"));
      ps.addValue("revenue", row.get("revenue"));
      ps.addValue("isClosed", isLastDayOfMonth);
      return ps;
    }).toArray(MapSqlParameterSource[]::new);

    jdbc.batchUpdate(upsertSql, batchParams);
    log.info("Gym metrics calculated for {} branches, month={}", results.size(), currentMonth);
  }
}
