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

  @Scheduled(cron = "0 0 6 1 * *", zone = "Europe/Madrid")
  public void generateMonthlyExpenseOccurrences() {
    var sql = """
        INSERT INTO expense_occurrences (expense_id, period, amount, amount_confirmed, payment_status, source)
        SELECT e.id, DATE_TRUNC('month', CURRENT_DATE)::date,
        CASE WHEN e.type = 'FIXED' THEN e.amount ELSE e.estimated_amount END,
        (e.type = 'FIXED'),
        'PENDING',
        'CRON'
        FROM expenses e
        WHERE e.recurrence = 'RECURRING'
          AND e.active IS TRUE
          AND NOT EXISTS (
            SELECT 1 FROM expense_occurrences eo
            WHERE eo.expense_id = e.id AND eo.period = DATE_TRUNC('month', CURRENT_DATE)::date
          )
        """;

    var rows = jdbc.update(sql, Collections.emptyMap());
    log.info("Generated {} expense occurrences for current month", rows);
  }
}
