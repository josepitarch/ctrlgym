package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Expense;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ExpenseRowMapper implements RowMapper<Expense> {

  @Override
  public Expense mapRow(ResultSet rs, int rowNum) throws SQLException {
    var expense = new Expense();
    expense.setId(rs.getLong("id"));
    expense.setGymBranchId(rs.getInt("gym_branch_id"));
    expense.setConcept(rs.getString("concept"));
    expense.setCategoryId(rs.getInt("category_id"));
    expense.setType(Expense.Type.from(rs.getString("type")));
    expense.setRecurrence(Expense.Recurrence.from(rs.getString("recurrence")));
    
    var amount = rs.getBigDecimal("amount");
    expense.setAmount(amount != null ? amount.doubleValue() : null);
    
    var expenseDate = rs.getDate("expense_date");
    expense.setExpenseDate(expenseDate != null ? expenseDate.toLocalDate() : null);
    
    var billingDay = rs.getObject("billing_day");
    expense.setBillingDay(billingDay != null ? ((Number) billingDay).shortValue() : null);
    
    var estimatedAmount = rs.getBigDecimal("estimated_amount");
    expense.setEstimatedAmount(estimatedAmount != null ? estimatedAmount.doubleValue() : null);
    
    expense.setActive(rs.getBoolean("active"));
    expense.setSource(Expense.Source.from(rs.getString("source")));
    
    return expense;
  }
}
