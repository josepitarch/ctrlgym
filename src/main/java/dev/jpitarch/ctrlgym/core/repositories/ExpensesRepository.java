package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.DatePeriod;
import dev.jpitarch.ctrlgym.core.domain.Expense;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ExpensesRepository {

  private final NamedParameterJdbcTemplate jdbc;

  public List<Expense> getExpenses(GymBranchId gymBranchId) {
    var sql = """
      SELECT category_id, nature, frequency, recurrence_period, expected_amount
      FROM expenses
      WHERE gym_branch_id = :gymBranchId
      AND start_date >= :from AND (end_date IS NULL OR end_date <= :to)
      """; //TODO: end_date is null???

    var params = Map.of(
      "gymBranchId", gymBranchId.branchId(),
      "from", LocalDate.of(Year.now().getValue(), 1, 1),
      "to", LocalDate.now()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var categoryId = row.getInt("category_id");
      var nature = Expense.Nature.from(row.getString("nature"));
      var frequency = Expense.Frequency.from(row.getString("frequency"));
      var recurrencePeriod = Expense.Recurrence.from(row.getString("recurrence_period"));
      var expectedAmount = row.getDouble("expected_amount");
      return new Expense(null, categoryId, nature, frequency, recurrencePeriod, expectedAmount);
    });
  }

  public List<Map<YearMonth, Double>> getTotalPerMonth(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      SELECT DATE_TRUNC('month', expo.occurrence_date)::date AS month, SUM(expo.amount) AS total_expenses
      FROM expenses exp
      JOIN expense_occurrences expo ON exp.id = expo.expense_id
      WHERE exp.gym_branch_id = :gymBranchId
      AND start_date >= :from AND (end_date IS NULL OR end_date <= :to)
      GROUP BY DATE_TRUNC('month', expo.occurrence_date)
      """;

    var params = Map.of(
      "gymBranchId", gymBranchId.branchId(),
      "from", datePeriod.from(),
      "to", datePeriod.to()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = row.getDate("month").toLocalDate();
      var totalExpenses = row.getDouble("total_expenses");
      return Map.of(YearMonth.from(month), totalExpenses);
    });
  }
}
