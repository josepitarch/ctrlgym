package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.dto.MembershipSeniorityDistribution;
import dev.jpitarch.ctrlgym.core.models.MembershipCancellationReasonTranslationMO;
import dev.jpitarch.ctrlgym.core.models.MembershipMO;
import dev.jpitarch.ctrlgym.core.models.MembershipPlanMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipCancellationReasonJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipPlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MembershipsRepository {

  private final MembershipJpaRepository membershipJpaRepository;

  private final MembershipPlanJpaRepository membershipPlanJpaRepository;

  private final MembershipCancellationReasonJpaRepository cancellationReasonJpaRepository;

  private final NamedParameterJdbcTemplate jdbc;

  public void save(Member.Id memberId, String membershipPlanId, String subscriptionId, LocalDate nextBillingDate) {
    var membership = new MembershipMO();
    membership.setMemberId(memberId.memberId());
    membership.setGymId(memberId.gymId());
    membership.setMembershipPlanId(membershipPlanId);
    membership.setStartDate(LocalDate.now());
    membership.setStripeSubscriptionId(subscriptionId);
    membership.setAutoRenew(true);
    membership.setNextBillingDate(nextBillingDate);

    membershipJpaRepository.save(membership);
  }

  public List<Membership> getMemberships(Member.Id memberId) {
    List<MembershipPlanMO> plans = membershipPlanJpaRepository.findByGymId(memberId.gymId());
    return membershipJpaRepository
      .findByMemberIdAndGymId(memberId.memberId(), memberId.gymId())
      .stream()
      .map(m -> Membership.builder()
        .id(m.getId().intValue())
        .interval(Membership.Recurring.from("MONTHLY")) //TODO
        .datePeriod(new DatePeriod(m.getStartDate(), m.getEndDate()))
        .nextBillingDate(m.getNextBillingDate())
        .build())
      .toList();
  }

  public void setCancellationReasonId(Member.Id memberId, String membershipId, Integer cancellationReasonId, String comment) {
    membershipJpaRepository
      .findByMemberIdAndGymIdAndMembershipPlanIdAndEndDateIsNull(memberId.memberId(), memberId.gymId(), membershipId)
      .ifPresent(m -> {
        m.setEndDate(LocalDate.now());
        m.setCancellationReasonId(cancellationReasonId);
        m.setCancellationComment(comment);
        membershipJpaRepository.save(m);
      });
  }

  public boolean hasActiveMembership(Member.Id memberId, String membershipId) {
    return membershipJpaRepository.hasActiveMembership(memberId.memberId(), memberId.gymId(), membershipId);
  }

  public List<Integer> getAccessibleBranches(Member.Id memberId) {
    var sql = """
      SELECT mpb.branch_id
      FROM memberships m
      INNER JOIN membership_plans mp ON m.membership_plan_id = mp.id
      INNER JOIN membership_plan_branches mpb ON mp.id = mpb.membership_plan_id
      WHERE m.member_id = :memberId AND mp.gym_id = :gymId
      AND m.start_date <= CURRENT_DATE AND (end_date IS NULL OR end_date >= CURRENT_DATE)
      """;

    var params = Map.of(
      "memberId", memberId.memberId(),
      "gymId", memberId.gymId()
    );

    return jdbc.queryForList(sql, params, Integer.class);
  }

  public List<MembershipCancellationReason> getCancellationReasons(String language) {
    return cancellationReasonJpaRepository.findByLanguageCode(language)
      .stream()
      .map(this::toDomain)
      .toList();
  }

  private MembershipCancellationReason toDomain(MembershipCancellationReasonTranslationMO translation) {
    return MembershipCancellationReason.builder()
      .id(translation.getCancellationReason().getId())
      .name(translation.getName())
      .description(translation.getDescription())
      .build();
  }

  public void createMembershipPlan(MembershipPlan membershipPlan, Integer gymId) {
    var sql = """
      INSERT INTO membership_plans (id, gym_id, name, stripe_price_id, price, billing_period, active, created_at)
      VALUES (:id, :gymId, :name, :priceId, :price, :billingPeriod, true, CURRENT_DATE)
      """;

    var params = Map.of(
      "id", membershipPlan.getId(),
      "gymId", gymId,
      "name", membershipPlan.getName(),
      "priceId", membershipPlan.getStripePriceId(),
      "price", membershipPlan.getPrice(),
      "billingPeriod", membershipPlan.getRecurring().name()
    );

    jdbc.update(sql, params);
  }

  public List<MembershipPlan> getMembershipPlans(Integer gymId) {
    return membershipPlanJpaRepository.findByGymId(gymId)
      .stream()
      .map(plan -> MembershipPlan.builder()
        .id(plan.getId())
        .name(plan.getName())
        .price(plan.getPrice().doubleValue())
        .recurring(Membership.Recurring.from(plan.getBillingPeriod()))
        .stripePriceId(plan.getStripePriceId())
        .build())
      .toList();
  }

  public void deleteMembershipPlan(String planId, Integer gymId) {
    MembershipPlanMO planMO = membershipPlanJpaRepository.findById(planId)
      .orElseThrow(() -> new IllegalArgumentException("Membership plan not found"));
    planMO.setDeletedAt(LocalDate.now());
    membershipPlanJpaRepository.save(planMO);
  }

  public String getStripePriceId(String id) {
    var sql = """
      SELECT stripe_price_id
      FROM membership_plans
      WHERE id = :id
      """;
    var params = Map.of("id", id);

    return jdbc.queryForObject(sql, params, String.class);
  }

  public String getStripeSubscriptionId(Member.Id memberId, String membershipId) {
    var sql = """
        SELECT stripe_subscription_id
        FROM memberships
        WHERE member_id = :memberId AND gym_id = :gymId AND membership_plan_id = :membershipId
      """;

    var params = Map.of(
      "memberId", memberId.memberId(),
      "gymId", memberId.gymId(),
      "membershipId", membershipId
    );

    return jdbc.queryForObject(sql, params, String.class);
  }

  public Map<YearMonth, Integer> getCurrentCount(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      SELECT
      month::date,
        COUNT(m.id) AS active_memberships
      FROM generate_series (
            :from::date,
            :to::date,
        INTERVAL '1 month'
      ) month
      LEFT JOIN memberships m
      ON m.gym_id = :gymId
      AND m.start_date <= month
      AND(m.end_date IS NULL OR m.end_date >= month)
      GROUP BY 1
      ORDER BY 1;
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId(),
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
      WITH months AS(
        SELECT generate_series(
          DATE_TRUNC('month', CAST(:from AS date)),
      DATE_TRUNC('month', CAST(:to AS date)),
      INTERVAL '1 month'
            )::date AS month
        ),
      memberships_by_month AS (
        SELECT
      DATE_TRUNC('month', start_date)::date AS month,
      COUNT( *)AS new_memberships
      FROM memberships
      WHERE gym_id = :gymId
      AND start_date >= :from
      AND start_date <= :to
      GROUP BY 1
        )
      SELECT
      m.month,
        COALESCE(mbm.new_memberships, 0) AS new_memberships
      FROM months m
      LEFT JOIN memberships_by_month mbm ON mbm.month = m.month
      ORDER BY m.month;
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId(),
      "from", datePeriod.from(),
      "to", datePeriod.to()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = row.getDate("month").toLocalDate();
      var newMemberships = row.getInt("new_memberships");
      return Map.entry(YearMonth.from(month), newMemberships);
    }).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public Map<YearMonth, Integer> getCancelledCount(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      WITH months AS(
        SELECT generate_series(
          DATE_TRUNC('month', CAST(:from AS date)),
          DATE_TRUNC('month', CAST(:to AS date)),
      INTERVAL '1 month'
            )::date AS month
        ),
      memberships_by_month AS (SELECT
      DATE_TRUNC('month', end_date)::date AS month,
      COUNT( *)AS cancellations
      FROM memberships
      WHERE gym_id = :gymId
      AND start_date >= :from AND start_date <= :to
      AND end_date IS NOT NULL AND end_date <= CURRENT_DATE
      GROUP BY 1
      ORDER BY 1
        )
      SELECT m.month, COALESCE(mbm.cancellations, 0) AS cancellations
      FROM months m
      LEFT JOIN memberships_by_month mbm ON mbm.month = m.month
      ORDER BY m.month;
      
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId(),
      "from", datePeriod.from(),
      "to", datePeriod.to()
    );

    return jdbc.query(sql, params, (row, _) -> {
      var month = row.getDate("month").toLocalDate();
      var cancellations = row.getInt("cancellations");
      return Map.entry(YearMonth.from(month), cancellations);
    }).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  }

  public MembershipSeniorityDistribution getSeniorityDistribution(GymBranchId gymBranchId) {
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
          WHERE membership_plan_id in (SELECT membership_plan_id FROM membership_plan_branches WHERE branch_id = :gymBranchId)
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

    var result = jdbc.query(sql, params, (row, _) -> Map.entry(row.getString("rango_antiguedad"), row.getInt("cantidad_membresias")));
    return new MembershipSeniorityDistribution(result.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
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
                                                     JOIN public.memberships mb
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

  public List<Map<String, Integer>> getCancellationReasons(GymBranchId gymBranchId, DatePeriod datePeriod) {
    var sql = """
      SELECT cancellation_reason_id, COUNT (1) as count
      FROM memberships
      WHERE gym_id = :gymId
      AND end_date <=CURRENT_DATE
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

}
