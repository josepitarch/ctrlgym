package dev.jpitarch.ctrlgym.repositories;

import dev.jpitarch.ctrlgym.domain.GymBranchId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ExpensesRepository {

  private final NamedParameterJdbcTemplate jdbc;

  public List<String[]> getExpenses(GymBranchId gymBranchId) {
    var sql = """
      SELECT nature, frequency, recurrence_period, expected_amount
      FROM expenses
      WHERE gym_branch_id = :gymBranchId
      AND start_date >= :from AND (end_date IS NULL OR end_date <= :to)
      """;

    var params = Map.of(
      "gymBranchId", gymBranchId.branchId(),
      "from", LocalDate.of(Year.now().getValue(), 1, 1),
      "to", LocalDate.now()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var nature = row.getString("nature");
      var frequency = row.getString("frequency");
      var recurrencePeriod = row.getString("recurrence_period");
      var expectedAmount = row.getDouble("expected_amount");
      return new String[]{ nature, frequency, recurrencePeriod, String.valueOf(expectedAmount) };
    });
  }
}
