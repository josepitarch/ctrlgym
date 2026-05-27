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
      SELECT DATE_TRUNC('month', issue_at)::date AS month, SUM(total) AS total_payments
      FROM invoices
      WHERE gym_id = :gymId AND issue_at BETWEEN :from AND :to
      GROUP BY DATE_TRUNC('month', issue_at)
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
