package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.DateRange;
import dev.jpitarch.ctrlgym.core.domain.Expense;
import dev.jpitarch.ctrlgym.core.entities.ExpenseCategoryEntity;
import dev.jpitarch.ctrlgym.core.entities.ExpenseEntity;
import dev.jpitarch.ctrlgym.core.mappers.ExpenseMapper;
import dev.jpitarch.ctrlgym.core.mappers.ExpenseRowMapper;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExpenseCategoryJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExpenseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ExpensesRepository {

  private final NamedParameterJdbcTemplate jdbc;

  private final ExpenseCategoryJpaRepository expenseCategoryJpaRepository;

  private final ExpenseJpaRepository expenseJpaRepository;

  private final ExpenseRowMapper expenseRowMapper;

  private final ExpenseMapper expenseMapper;

  public List<ExpenseCategoryEntity> getAllCategories(Integer gymId) {
    return expenseCategoryJpaRepository.findAllByGymId(gymId);
  }

  public List<Expense> getExpenses(Integer gymBranchId, YearMonth month) {
    var sql = """
      SELECT id, gym_branch_id, concept, category_id, type, recurrence, amount, expense_date, billing_day, estimated_amount, active, source
      FROM expenses
      WHERE gym_branch_id = :gymBranchId
        AND active IS true
        AND recurrence = 'ONE_OFF'
        AND DATE_TRUNC('month', expense_date) = CAST(:month AS date)

      UNION ALL

      SELECT e.id, e.gym_branch_id, e.concept, e.category_id, e.type, e.recurrence, eo.amount, e.expense_date, e.billing_day, e.estimated_amount, e.active, e.source
      FROM expenses e
      JOIN expense_occurrences eo ON e.id = eo.expense_id
      WHERE e.gym_branch_id = :gymBranchId
        AND e.active IS true
        AND e.recurrence = 'RECURRING'
        AND DATE_TRUNC('month', eo.period) = CAST(:month AS date)
      """;

    var params = Map.of("gymBranchId", gymBranchId, "month", month.atDay(1));

    return jdbc.query(sql, params, expenseRowMapper);
  }

  public boolean existsByCategoryId(Integer categoryId) {
    var sql = "SELECT COUNT(*) > 0 FROM expenses WHERE category_id = :categoryId";
    var params = Map.of("categoryId", categoryId);
    return Boolean.TRUE.equals(jdbc.queryForObject(sql, params, Boolean.class));
  }

  public Expense createExpense(Expense expense, Integer gymBranchId) {
    ExpenseEntity entity = expenseMapper.toEntity(expense);
    entity.setGymBranchId(gymBranchId);
    ExpenseEntity saved = expenseJpaRepository.save(entity);
    expense.setId(saved.getId());
    return expense;
  }

  public void deleteExpense(Long expenseId) {
    expenseJpaRepository.deleteById(expenseId);
  }

  public Map<YearMonth, Double> getTotalPerMonth(Integer gymBranchId, DateRange dateRange) {
    var sql = """
      WITH months AS (
          SELECT generate_series(
              date_trunc('month', CAST(:from AS date)),
              date_trunc('month', CAST(:to AS date)),
              INTERVAL '1 month'
          )::date AS month
      ),
      expense_data AS (
          SELECT
              DATE_TRUNC('month', exp.expense_date) AS month,
              exp.amount
          FROM expenses exp
          WHERE exp.gym_branch_id = :gymBranchId
            AND exp.active = true
            AND exp.recurrence = 'ONE_OFF'
            AND exp.expense_date >= :from
            AND exp.expense_date <= :to

          UNION ALL

          SELECT
              eo.period AS month,
              eo.amount
          FROM expenses exp
          JOIN expense_occurrences eo ON exp.id = eo.expense_id
          WHERE exp.gym_branch_id = :gymBranchId
            AND exp.active = true
            AND exp.recurrence = 'RECURRING'
            AND eo.period >= :from
            AND eo.period <= :to
      )
      SELECT
          m.month AS month,
          COALESCE(SUM(ed.amount), 0) AS total_expenses
      FROM months m
      LEFT JOIN expense_data ed ON ed.month = m.month
      GROUP BY m.month
      ORDER BY m.month;
      """;

    var params = Map.of(
      "gymBranchId", gymBranchId,
      "from", dateRange.from(),
      "to", dateRange.to()
    );

    return jdbc.query(sql, params, (row, rowNum) -> {
      var month = row.getDate("month").toLocalDate();
      var totalExpenses = row.getDouble("total_expenses");
      return Map.entry(YearMonth.from(month), totalExpenses);
    }).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
