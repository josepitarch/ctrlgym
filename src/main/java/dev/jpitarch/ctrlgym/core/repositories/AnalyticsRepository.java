package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Cohort;
import dev.jpitarch.ctrlgym.core.domain.DatePeriod;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.dto.BranchMetrics;
import dev.jpitarch.ctrlgym.core.dto.MembersDistribution;
import dev.jpitarch.ctrlgym.core.dto.RetentionVsChurn;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AnalyticsRepository {

  private final NamedParameterJdbcTemplate jdbc;

  public Map<YearMonth, Integer> getCurrentCount(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      SELECT
      month::date,
        COUNT(branch_m.id) AS active_memberships
      FROM generate_series (
            :from::date,
            :to::date,
        INTERVAL '1 month'
      ) month
      LEFT JOIN memberships branch_m ON branch_m.gym_id = :gymId
        AND branch_m.start_date <= month AND (branch_m.end_date IS NULL OR branch_m.end_date >= month)
        AND branch_m.membership_plan_id IN (SELECT mp.id FROM membership_plans mp WHERE mp.gym_branch_id = :gymBranchId)
      GROUP BY 1
      ORDER BY 1;
      """;

    var params = Map.of(
            "gymId", gymBranchId.gymId(),
            "gymBranchId", gymBranchId.branchId(),
            "from", datePeriod.from().toString(),
            "to", datePeriod.to().toString()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = row.getDate("month").toLocalDate();
      var activeMemberships = row.getInt("active_memberships");
      return Map.entry(YearMonth.from(month), activeMemberships);
    }).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  }

  public Map<YearMonth, Integer> getNewsCount(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      SELECT
      month::date,
        COUNT(branch_m.id) AS new_memberships
      FROM generate_series (
            :from::date,
            :to::date,
        INTERVAL '1 month'
      ) month
      LEFT JOIN memberships branch_m ON branch_m.gym_id = :gymId
        AND DATE_TRUNC('month', branch_m.start_date)::date = month
        AND branch_m.membership_plan_id IN (
          SELECT mp.id FROM membership_plans mp WHERE mp.gym_branch_id = :gymBranchId
        )
      GROUP BY 1
      ORDER BY 1;
      """;

    var params = Map.of(
            "gymId", gymBranchId.gymId(),
            "gymBranchId", gymBranchId.branchId(),
            "from", datePeriod.from().toString(),
            "to", datePeriod.to().toString()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = row.getDate("month").toLocalDate();
      var newMemberships = row.getInt("new_memberships");
      return Map.entry(YearMonth.from(month), newMemberships);
    }).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public Map<YearMonth, Integer> getCancelledCount(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      SELECT
      month::date,
        COUNT(branch_m.id) AS cancellations
      FROM generate_series (
            :from::date,
            :to::date,
        INTERVAL '1 month'
      ) month
      LEFT JOIN memberships branch_m ON branch_m.gym_id = :gymId
        AND branch_m.end_date IS NOT NULL
        AND DATE_TRUNC('month', branch_m.end_date)::date = month
        AND branch_m.membership_plan_id IN (
          SELECT mp.id FROM membership_plans mp WHERE mp.gym_branch_id = :gymBranchId
        )
      GROUP BY 1
      ORDER BY 1;
      """;

    var params = Map.of(
            "gymId", gymBranchId.gymId(),
            "gymBranchId", gymBranchId.branchId(),
            "from", datePeriod.from().toString(),
            "to", datePeriod.to().toString()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = row.getDate("month").toLocalDate();
      var cancellations = row.getInt("cancellations");
      return Map.entry(YearMonth.from(month), cancellations);
    }).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  }

  public List<Object[]> getSeniorityDistribution(GymBranchId gymBranchId) {
    var sql = """
      WITH membresias_vigentes AS (
          SELECT
              id,
              member_id,
              gym_id,
              start_date,
              (DATE_PART('year', AGE(CURRENT_DATE, start_date)) * 12
                  + DATE_PART('month', AGE(CURRENT_DATE, start_date)))::int AS meses_antiguedad
          FROM public.memberships
          WHERE membership_plan_id in (SELECT membership_plan_id FROM membership_plans WHERE gym_branch_id = :gymBranchId)
          AND (end_date IS NULL OR end_date >= CURRENT_DATE)
      )
      SELECT
          CASE
              WHEN meses_antiguedad < 1              THEN '-1m'
              WHEN meses_antiguedad BETWEEN 1 AND 3   THEN '1-3'
              WHEN meses_antiguedad BETWEEN 4 AND 5   THEN '4-5m'
              WHEN meses_antiguedad BETWEEN 6 AND 11  THEN '6-12m'
              WHEN meses_antiguedad BETWEEN 12 AND 23 THEN '1-2y'
              WHEN meses_antiguedad BETWEEN 24 AND 35 THEN '2-3y'
              WHEN meses_antiguedad >= 36              THEN '+3y'
              END AS rango_antiguedad,
          COUNT(*) AS cantidad_membresias
      FROM membresias_vigentes
      GROUP BY rango_antiguedad
      ORDER BY MIN(meses_antiguedad);
      """;

    var params = Map.of("gymBranchId", gymBranchId.branchId());

    var result = jdbc.query(sql, params, (row, _) -> new Object[]{ row.getString("rango_antiguedad"), row.getInt("cantidad_membresias") });
    return result;
  }

  public Map<YearMonth, Integer> getSeniorityAverage(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      WITH months AS (SELECT generate_series(
                                     date_trunc('month', CAST(:from AS date)),
                                     date_trunc('month', CAST(:to AS date)),
                                     INTERVAL '1 month'
                             )::date AS month_start),
           active_memberships_per_month AS (SELECT m.month_start,
                                                   mb.id,
                                                   (DATE_PART('year', AGE(
                                                           (m.month_start + INTERVAL '1 month' - INTERVAL '1 day')::date,
                                                           mb.start_date)) * 12
                                                       + DATE_PART('month', AGE(
                                                               (m.month_start + INTERVAL '1 month' - INTERVAL '1 day')::date,
                                                               mb.start_date)))::int AS tenure_months
                                            FROM months m
                                                     LEFT JOIN public.memberships mb
                                                          ON mb.start_date <=
                                                             (m.month_start + INTERVAL '1 month' - INTERVAL '1 day')::date
                                                              AND (mb.end_date IS NULL OR mb.end_date >=
                                                                                          (m.month_start + INTERVAL '1 month' - INTERVAL '1 day')::date))
      SELECT TO_CHAR(month_start, 'YYYY-MM') AS month,
             ROUND(AVG(tenure_months), 1)    AS average_tenure_months
      FROM active_memberships_per_month
      GROUP BY month_start
      ORDER BY month_start;
      
      """;

    var params = Map.of(
            "gymId", gymBranchId.gymId(),
            "from", datePeriod.from(),
            "to", datePeriod.to()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = YearMonth.parse(row.getString("month"));
      var averageTenure = (int) Math.round(row.getDouble("average_tenure_months"));
      return Map.entry(month, averageTenure);
    }).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public List<Cohort> getCohorts(GymBranchId gymBranchId) {
    var sql = """
      WITH cohorts AS(
        SELECT
        id,
        DATE_TRUNC('month', start_date)::date AS cohort_month,
        start_date,
        end_date
        FROM memberships
        WHERE gym_id = :gymId
        ),
      
      cohort_activity AS (
        SELECT
      c.cohort_month,
        gs.month_offset,
        COUNT( *)FILTER(
        WHERE c.end_date IS NULL
        OR c.end_date >= c.cohort_month
          + (gs.month_offset || ' month')::interval
      ) AS active_members
      FROM cohorts c
      CROSS JOIN generate_series(0, :currentMonth)AS gs (month_offset)
        GROUP BY 1, 2
        ),
      
      cohort_sizes AS (
        SELECT
      cohort_month,
        COUNT( *)AS cohort_size
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
      JOIN cohort_sizes cs ON cs.cohort_month = ca.cohort_month
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
      return new Cohort(YearMonth.from(cohortMonth), monthOffset, activeMembers, cohortSize, retentionRate);
    });

  }

  public RetentionVsChurn getRetentionVsChurn(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      WITH months AS (
          SELECT generate_series(
                         date_trunc('month', CAST(:from AS date)),
                         date_trunc('month', CAST(:to AS date)),
                         INTERVAL '1 month'
                  )::date AS month_start
      ),
           monthly_stats AS (
               SELECT
                   m.month_start,
                   COUNT(*) FILTER (
                       WHERE mb.start_date <= m.month_start
                           AND (mb.end_date IS NULL OR mb.end_date >= m.month_start)
                       ) AS active_at_start,
                   COUNT(*) FILTER (
                       WHERE mb.start_date <= m.month_start
                           AND mb.end_date >= m.month_start
                           AND mb.end_date < (m.month_start + INTERVAL '1 month')::date
                       ) AS churned_during_month
               FROM months m
                         LEFT JOIN public.memberships mb
                                   ON mb.gym_id = :gymId
               GROUP BY m.month_start
           )
      SELECT
          TO_CHAR(month_start, 'YYYY-MM') AS month,
          CASE WHEN active_at_start > 0
                   THEN ROUND(100.0 * churned_during_month / active_at_start, 1)
               ELSE 0
              END AS churn_percentage,
          CASE WHEN active_at_start > 0
                   THEN ROUND(100.0 - (100.0 * churned_during_month / active_at_start), 1)
               ELSE 0
              END AS retention_percentage
      FROM monthly_stats
      ORDER BY month_start;
      """;

    var params = Map.of(
            "gymId", gymBranchId.gymId(),
            "from", datePeriod.from(),
            "to", datePeriod.to()
    );

    var results = jdbc.query(sql, params, (row, _) -> {
      var month = YearMonth.parse(row.getString("month"));
      double churnPercentage = row.getDouble("churn_percentage");
      double retentionPercentage = row.getDouble("retention_percentage");
      return Map.entry(month, new Double[]{ retentionPercentage, churnPercentage });
    });

    var retention = results.stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[0]));
    var churn = results.stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()[1]));

    return new RetentionVsChurn(retention, churn);
  }

  public List<Map<String, Integer>> getCancellationReasons(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      SELECT cancellation_reason_id, COUNT (1) as count
      FROM memberships
      WHERE gym_id = :gymId
      AND end_date <= CURRENT_DATE
      GROUP BY cancellation_reason_id
      """;

    var params = Map.of(
            "gymId", gymBranchId.gymId()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var reasonId = row.getInt("cancellation_reason_id");
      var count = row.getInt("count");
      return Map.of(
              "id", reasonId,
              "count", count
      );
    });
  }

  public Map<MembersDistribution.Group, List<String[]>> getDistribution(GymBranchId gymBranchId) {
    var sql = """
      SELECT
        gender,
        postal_code,
        CASE
          WHEN EXTRACT(YEAR FROM AGE(birth_date)) BETWEEN 18 AND 25 THEN '18-25'
          WHEN EXTRACT(YEAR FROM AGE(birth_date)) BETWEEN 26 AND 35 THEN '26-35'
          WHEN EXTRACT(YEAR FROM AGE(birth_date)) BETWEEN 36 AND 45 THEN '36-45'
          ELSE '+45'
        END AS age_range,
        COUNT(*) AS total
      FROM members m
      WHERE m.gym_id = :gymId
      AND EXISTS (
          SELECT 1
          FROM memberships mb
          JOIN membership_plans mp on mb.membership_plan_id = mp.id
          WHERE m.id = mb.member_id AND m.gym_id = mb.gym_id AND mp.gym_branch_id = :gymBranchId
          AND mb.start_date <= CURRENT_DATE AND (mb.end_date IS NULL OR mb.end_date > CURRENT_DATE)
      )
      GROUP BY GROUPING SETS (
        (gender),
        (postal_code),
        (age_range)
      );
      """;

    var params = Map.of(
            "gymId", gymBranchId.gymId(),
            "gymBranchId", gymBranchId.branchId()
    );

    return jdbc.query(sql, params, (rs, _) -> new String[]{
            rs.getString("gender"),
            rs.getString("postal_code"),
            rs.getString("age_range"),
            String.valueOf(rs.getInt("total"))
    }).stream().collect(Collectors.groupingBy(
            row -> {
              if (row[0] != null) return MembersDistribution.Group.GENDER;
              if (row[1] != null) return MembersDistribution.Group.POSTAL_CODE;
              if (row[2] != null) return MembersDistribution.Group.AGE;
              throw new RuntimeException("No distribution gender or postal code provided");
            }, Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> list.stream()
                            .map(arr -> Arrays.stream(arr)
                                    .filter(Objects::nonNull)
                                    .toArray(String[]::new))
                            .toList())
    ));
  }

  public List<BranchMetrics> getMonthlyMetrics(Integer gymId, YearMonth from, YearMonth to) {
    var sql = """
      SELECT
        gb.id AS branch_id,
        gb.name AS branch_name,
        TO_CHAR(month, 'YYYY-MM') AS year_month,
        COALESCE(gmm.revenue, 0) AS revenue,
        COALESCE(gmm.expense, 0) AS expense,
        COALESCE(gmm.active_members, 0)::smallint AS active_members,
        COALESCE(gmm.new_members, 0)::smallint AS new_members,
        COALESCE(gmm.churned_members, 0)::smallint AS churned_members,
        gmm.churn_rate,
        gmm.peak_occupancy_pct,
        COALESCE(gmm.overdue_amount, 0) AS overdue_amount,
        COALESCE(gmm.is_closed, false) AS is_closed
      FROM generate_series(
        :from::date,
        :to::date,
        INTERVAL '1 month'
      ) AS month
      CROSS JOIN gym_branches gb
      LEFT JOIN gym_metrics_monthly gmm
        ON gmm.gym_branch_id = gb.id
        AND gmm.year_month = month::date
      WHERE gb.gym_id = :gymId
      ORDER BY month, gb.id;
      """;

    var params = Map.of(
            "gymId", gymId,
            "from", from.atDay(1).toString(),
            "to", to.atDay(1).toString()
    );

    return jdbc.query(sql, params, (rs, _) -> new BranchMetrics(
            rs.getInt("branch_id"),
            rs.getString("branch_name"),
            YearMonth.parse(rs.getString("year_month")),
            rs.getBigDecimal("revenue"),
            rs.getBigDecimal("expense"),
            rs.getShort("active_members"),
            rs.getShort("new_members"),
            rs.getShort("churned_members"),
            rs.getObject("churn_rate") != null ? rs.getBigDecimal("churn_rate") : null,
            rs.getObject("peak_occupancy_pct") != null ? rs.getBigDecimal("peak_occupancy_pct") : null,
            rs.getBigDecimal("overdue_amount"),
            rs.getBoolean("is_closed")
    ));
  }

}
