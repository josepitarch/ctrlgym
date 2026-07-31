package dev.jpitarch.ctrlgym.crons;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class ExpenseOccurrencesCron {

  private final NamedParameterJdbcTemplate jdbc;

  @Scheduled(cron = "0 0 6 1 * *")
  public void generateMonthlyExpenseOccurrences() {
    var sql = """
        INSERT INTO expense_occurrences (expense_id, occurrence_date, amount)
        SELECT e.id, CURRENT_DATE, e.expected_amount
        FROM expenses e
        WHERE e.frequency = 'RECURRING'
          AND e.nature = 'FIXED'
          AND e.status = 'ACTIVE'
          AND e.recurrence_period = 'MONTHLY'
          AND e.start_date <= CURRENT_DATE AND (e.end_date IS NULL OR e.end_date >= CURRENT_DATE)
          AND NOT EXISTS (
            SELECT 1 FROM expense_occurrences eo
            WHERE eo.expense_id = e.id AND eo.occurrence_date = CURRENT_DATE
          )
        """;

    var rows = jdbc.update(sql, Collections.emptyMap());
    log.info("Generated {} expense occurrences for current month", rows);
  }
}
