package dev.jpitarch.ctrlgym.crons;

import dev.jpitarch.ctrlgym.core.controllers.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CronTestIT extends BaseIntegrationTest {

  @Autowired
  private NamedParameterJdbcTemplate jdbc;

  @Autowired
  private GymMetricsCron gymMetricsCron;

  @Autowired
  private MemberMetricsCron memberMetricsCron;

  private static final UUID NEW_MEMBER_ID = UUID.fromString("e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1");
  private static final UUID CHURNED_MEMBER_ID = UUID.fromString("f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2");
  private static final UUID EXTERNAL_MEMBER_ID = UUID.fromString("a3a3a3a3-a3a3-a3a3-a3a3-a3a3a3a3a3a3");
  private static final UUID EXISTING_MEMBER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

  private static boolean dataPopulated = false;

  @BeforeEach
  void populateData() {
    if (dataPopulated) {
      return;
    }
    var ym = YearMonth.now();
    var monthStart = ym.atDay(1);
    var midMonth = ym.atDay(15);
    var prevMonthEnd = ym.minusMonths(1).atEndOfMonth();
    var prevMonthStart = ym.minusMonths(1).atDay(1);
    var jan1 = LocalDate.of(2026, 1, 1);

    jdbc.update("DELETE FROM member_accesses WHERE member_id IN (:m1, :m2, :m3, :m4)",
      Map.of("m1", NEW_MEMBER_ID, "m2", CHURNED_MEMBER_ID, "m3", EXTERNAL_MEMBER_ID, "m4", EXISTING_MEMBER_ID));
    jdbc.update("DELETE FROM invoices WHERE id IN ('INV-CRON-001', 'INV-CRON-002', 'INV-CRON-003')", Map.of());
    jdbc.update("DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE series = 'CRON')", Map.of());
    jdbc.update("DELETE FROM orders WHERE series = 'CRON'", Map.of());
    jdbc.update("DELETE FROM expenses WHERE concept LIKE 'Cron test%'", Map.of());
    jdbc.update("DELETE FROM expense_occurrences WHERE expense_id IN (SELECT id FROM expenses WHERE concept LIKE 'Cron test%')", Map.of());
    jdbc.update("DELETE FROM memberships WHERE member_id IN (:m1, :m2, :m3)",
      Map.of("m1", NEW_MEMBER_ID, "m2", CHURNED_MEMBER_ID, "m3", EXTERNAL_MEMBER_ID));
    jdbc.update("DELETE FROM users WHERE id IN (:m1, :m2, :m3)",
      Map.of("m1", NEW_MEMBER_ID, "m2", CHURNED_MEMBER_ID, "m3", EXTERNAL_MEMBER_ID));
    jdbc.update("DELETE FROM gym_metrics_monthly WHERE gym_branch_id = 1", Map.of());
    jdbc.update("DELETE FROM member_metrics_monthly WHERE member_id IN (:m1, :m2, :m3, :m4)",
      Map.of("m1", NEW_MEMBER_ID, "m2", CHURNED_MEMBER_ID, "m3", EXTERNAL_MEMBER_ID, "m4", EXISTING_MEMBER_ID));

    jdbc.update(
      """
        INSERT INTO users (id, gym_id, email, name, first_surname, status, role, nif, created_at)
        VALUES
          (:newMember, 1, 'new.cron@example.com', 'New', 'Cron', 'ACTIVE', 'MEMBER', 'NEWCRON1', now()),
          (:churnedMember, 1, 'churned.cron@example.com', 'Churned', 'Cron', 'ACTIVE', 'MEMBER', 'CHNCRO2', now()),
          (:externalMember, 1, 'external.cron@example.com', 'External', 'Cron', 'ACTIVE', 'MEMBER', 'EXTCRON3', now())
      """,
      Map.of("newMember", NEW_MEMBER_ID, "churnedMember", CHURNED_MEMBER_ID, "externalMember", EXTERNAL_MEMBER_ID)
    );

    jdbc.update(
      """
        INSERT INTO memberships (member_id, gym_id, membership_plan_id, start_date, end_date, auto_renew)
        VALUES
          (:newMember, 1, 'plan_basic', :monthStart, NULL, true),
          (:churnedMember, 1, 'plan_premium', :jan1, :midMonth, false),
          (:externalMember, 1, 'plan_basic', :jan1, :prevMonthEnd, false)
      """,
      Map.of(
        "newMember", NEW_MEMBER_ID,
        "churnedMember", CHURNED_MEMBER_ID,
        "externalMember", EXTERNAL_MEMBER_ID,
        "monthStart", monthStart,
        "midMonth", midMonth,
        "jan1", jan1,
        "prevMonthEnd", prevMonthEnd
      )
    );

    jdbc.update(
      """
        INSERT INTO invoices (id, gym_id, member_id, series, "number", issue_at, due_at, status, subtotal, tax, total, membership_id)
        VALUES
          ('INV-CRON-001', 1, :existingMember, 'CRON', '001', :monthStart, :monthStart, 'PAID', 49.99, 0, 49.99, 1),
          ('INV-CRON-002', 1, :newMember, 'CRON', '002', :monthStart, :midMonth, 'FAILED', 29.99, 0, 29.99, 2),
          ('INV-CRON-003', 1, :externalMember, 'CRON', '003', :prevMonthStart, :prevMonthEnd, 'FAILED', 29.99, 0, 29.99, 3)
      """,
      Map.of(
        "existingMember", EXISTING_MEMBER_ID,
        "newMember", NEW_MEMBER_ID,
        "externalMember", EXTERNAL_MEMBER_ID,
        "monthStart", monthStart,
        "midMonth", midMonth,
        "prevMonthStart", prevMonthStart,
        "prevMonthEnd", prevMonthEnd
      )
    );

    jdbc.update(
      """
        INSERT INTO orders (id, gym_id, member_id, gym_branch_id, series, "number", created_at)
        OVERRIDING SYSTEM VALUE
        VALUES
          (100, 1, :existingMember, 1, 'CRON', '100', :midMonth),
          (101, 1, :newMember, 1, 'CRON', '101', :midMonth)
      """,
      Map.of(
        "existingMember", EXISTING_MEMBER_ID,
        "newMember", NEW_MEMBER_ID,
        "midMonth", midMonth
      )
    );

    jdbc.update(
      """
        INSERT INTO order_items (order_id, product_id, product_name_snapshot, product_price_snapshot, quantity)
        VALUES
          (100, 1, 'Protein Shake', 10.00, 2),
          (100, 2, 'Gym Towel', 5.00, 1),
          (101, 1, 'Protein Shake', 15.00, 1)
      """,
      Map.of()
    );

    jdbc.update(
      """
        INSERT INTO expenses (gym_branch_id, concept, category_id, type, recurrence, amount, expense_date, active, source)
        VALUES
          (1, 'Cron test one-off', 1, 'VARIABLE', 'ONE_OFF', 200.00, :monthStart, true, 'MANUAL'),
          (1, 'Cron test recurring', 1, 'FIXED', 'RECURRING', NULL, NULL, true, 'MANUAL')
      """,
      Map.of("monthStart", monthStart)
    );

    jdbc.update(
      """
        INSERT INTO expense_occurrences (expense_id, period, amount, payment_status, source)
        VALUES (2, :monthStart, 150.00, 'PAID', 'MANUAL')
      """,
      Map.of("monthStart", monthStart)
    );

    var accessDates = new LocalDate[]{
      ym.atDay(2), ym.atDay(5), ym.atDay(10), ym.atDay(15), ym.atDay(20)
    };
    for (var d : accessDates) {
      jdbc.update(
        """
          INSERT INTO member_accesses (gym_branch_id, member_id, direction, created_at, gym_id)
          VALUES (1, :member, 0, :ts, 1)
        """,
        Map.of("member", EXISTING_MEMBER_ID, "ts", Timestamp.from(d.atStartOfDay().toInstant(ZoneOffset.UTC)))
      );
    }

    var newMemberDates = new LocalDate[]{
      ym.atDay(6), ym.atDay(12), ym.atDay(18)
    };
    for (var d : newMemberDates) {
      jdbc.update(
        """
          INSERT INTO member_accesses (gym_branch_id, member_id, direction, created_at, gym_id)
          VALUES (1, :member, 0, :ts, 1)
        """,
        Map.of("member", NEW_MEMBER_ID, "ts", Timestamp.from(d.atStartOfDay().toInstant(ZoneOffset.UTC)))
      );
    }

    jdbc.update(
      """
        INSERT INTO member_accesses (gym_branch_id, member_id, direction, created_at, gym_id)
        VALUES (1, :member, 0, :ts, 1)
      """,
      Map.of("member", CHURNED_MEMBER_ID, "ts", Timestamp.from(ym.atDay(3).atStartOfDay().toInstant(ZoneOffset.UTC)))
    );
    dataPopulated = true;
  }

  @Test
  @Order(1)
  @DisplayName("GymMetricsCron calculates metrics correctly")
  void gymMetricsCron_calculatesCorrectly() {
    gymMetricsCron.calculateMonthlyMetrics();

    var yearMonth = YearMonth.now().atDay(1);
    var result = jdbc.queryForMap(
      "SELECT * FROM gym_metrics_monthly WHERE gym_branch_id = 1 AND year_month = :ym",
      Map.of("ym", yearMonth)
    );

    assertThat(((Number) result.get("active_members")).intValue()).isEqualTo(3);
    assertThat(((Number) result.get("new_members")).intValue()).isEqualTo(1);
    assertThat(((Number) result.get("churned_members")).intValue()).isEqualTo(1);
    assertThat(new BigDecimal(result.get("revenue").toString())).isEqualByComparingTo("119.98");
    assertThat(new BigDecimal(result.get("expense").toString())).isEqualByComparingTo("350.00");
    assertThat(new BigDecimal(result.get("overdue_amount").toString())).isEqualByComparingTo("29.99");
    assertThat(new BigDecimal(result.get("churn_rate").toString())).isEqualByComparingTo("25.00");
  }

  @Test
  @Order(2)
  @DisplayName("MemberMetricsCron calculates attendance correctly")
  void memberMetricsCron_calculatesAttendance() {
    memberMetricsCron.calculateMonthlyMetrics();

    var yearMonth = YearMonth.now().atDay(1);

    var existingMember = jdbc.queryForMap(
      "SELECT * FROM member_metrics_monthly WHERE member_id = :id AND year_month = :ym",
      Map.of("id", EXISTING_MEMBER_ID, "ym", yearMonth)
    );
    assertThat(((Number) existingMember.get("attendance")).intValue()).isEqualTo(5);

    var newMember = jdbc.queryForMap(
      "SELECT * FROM member_metrics_monthly WHERE member_id = :id AND year_month = :ym",
      Map.of("id", NEW_MEMBER_ID, "ym", yearMonth)
    );
    assertThat(((Number) newMember.get("attendance")).intValue()).isEqualTo(3);

    var churnedMember = jdbc.queryForMap(
      "SELECT * FROM member_metrics_monthly WHERE member_id = :id AND year_month = :ym",
      Map.of("id", CHURNED_MEMBER_ID, "ym", yearMonth)
    );
    assertThat(((Number) churnedMember.get("attendance")).intValue()).isEqualTo(1);
  }
}
