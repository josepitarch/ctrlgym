package dev.jpitarch.ctrlgym.core.repositories;


import dev.jpitarch.ctrlgym.core.domain.DatePeriod;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class InvoicesRepository {

  private final NamedParameterJdbcTemplate jdbc;

  public List<Map<YearMonth, Double>> getTotalPerMonth(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      
       WITH months AS (
          SELECT generate_series(
              date_trunc('month', CAST(:from AS date)),
              date_trunc('month', CAST(:to AS date)),
              INTERVAL '1 month'
          )::date AS month
      ),
      invoice_data AS (
          SELECT
              DATE_TRUNC('month', issue_at)::date AS month,
              total
          FROM invoices
          WHERE gym_id = :gymId
            AND issue_at BETWEEN :from AND :to
      )
      SELECT
          m.month AS month,
          COALESCE(SUM(inv.total), 0) AS total_payments
      FROM months m
      LEFT JOIN invoice_data inv ON inv.month = m.month
      GROUP BY m.month
      ORDER BY m.month;
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId(),
      "from", datePeriod.from(),
      "to", datePeriod.to()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = row.getDate("month").toLocalDate();
      var totalPayments = row.getDouble("total_payments");
      return Map.of(YearMonth.from(month), totalPayments);
    });
  }
}
