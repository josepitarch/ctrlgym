package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.enums.MemberDistribution;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MembersRepository {

  private final NamedParameterJdbcTemplate jdbc;

  private final MemberAccessJpaRepository memberAccessJpaRepository;

  public Member getById(UUID id) {
    var sql = """
      SELECT *
      FROM members
      WHERE id = :id
      """;

    var params = Map.of("id", id);

    return jdbc.queryForObject(sql, params, Member.class);
  }

  public Optional<String> getStripeCustomerId(UUID id) {
    var sql = """
      SELECT stripe_customer_id
      FROM members
      WHERE id = :id
      """;

    var params = Map.of("id", id);

    return Optional.ofNullable(this.jdbc.queryForObject(sql, params, String.class));
  }

  public UUID getId(String stripeCustomerId) {
    var sql = """
        SELECT id
        FROM members
        WHERE stripe_customer_id = :stripeCustomerId
      """;

    var params = Map.of("stripeCustomerId", stripeCustomerId);

    return this.jdbc.queryForObject(sql, params, UUID.class);

  }

  public Optional<String> getPaymentMethodId(UUID id) {
    var sql = """
      SELECT stripe_payment_method_id
      FROM members
      WHERE id = :id
      """;

    var params = Map.of("id", id);

    return Optional.ofNullable(this.jdbc.queryForObject(sql, params, String.class));
  }

  public Integer getGymId(UUID id) {
    var sql = """
        SELECT gym_id
        FROM members
        WHERE id = :id
      """;

    var params = Map.of("id", id);

    return this.jdbc.queryForObject(sql, params, Integer.class);
  }

  public void saveCustomerId(UUID id, String customerId) {
    var sql = """
      UPDATE members
      SET stripe_customer_id = :customerId
      WHERE id = :id
      """;

    var params = Map.of(
      "id", id,
      "customerId", customerId
    );

    this.jdbc.update(sql, params);
  }

  public void savePaymentMethodId(String customerId, String paymentMethodId) {
    var sql = """
      UPDATE members
      SET stripe_payment_method_id = :paymentMethodId
      WHERE stripe_customer_id = :customerId
      """;

    var params = Map.of(
      "customerId", customerId,
      "paymentMethodId", paymentMethodId
    );

    this.jdbc.update(sql, params);
  }

  public List<Member> getMembers(GymBranchId gymBranchId) {
    var sql = """
      SELECT m.id, m.name, m.first_surname, m.second_surname, m.email, m.gender, m.birth_date, m.postal_code, m.gym_id
      FROM members m
      JOIN memberships ms ON m.id = ms.member_id
      JOIN membership_plan_branches mpb ON ms.membership_plan_id = mpb.membership_plan_id
      WHERE m.gym_id = :gymId AND mpb.branch_id = :gymBranchId
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId(),
      "gymBranchId", gymBranchId.branchId()
    );

    return jdbc.queryForList(sql, params, Member.class);
  }

  public Map<MemberDistribution, List<String[]>> getDistribution(GymBranchId gymBranchId) {
    var sql = """
      SELECT
        gender,
        postal_code,
        CASE
          WHEN EXTRACT(YEAR FROM AGE(birth_date)) BETWEEN 18 AND 25 THEN '18-25'
          WHEN EXTRACT(YEAR FROM AGE(birth_date)) BETWEEN 26 AND 35 THEN '26-35'
          WHEN EXTRACT(YEAR FROM AGE(birth_date)) BETWEEN 36 AND 45 THEN '36-45'
          ELSE '+45'
        END AS age_range,
        COUNT(*) AS total
      FROM members
      GROUP BY GROUPING SETS (
        (gender),
        (postal_code),
        (age_range)
      );
      """;

    return jdbc.query(sql, (rs, rowNum) -> new String[]{
      rs.getString("gender"),
      rs.getString("postal_code"),
      rs.getString("age_range"),
      String.valueOf(rs.getInt("total"))
    }).stream().collect(Collectors.groupingBy(
      row -> {
        if (row[0] != null) return MemberDistribution.GENDER;
        if (row[1] != null) return MemberDistribution.POSTAL_CODE;
        if (row[2] != null) return MemberDistribution.AGE;
        throw new RuntimeException("No distribution gender or postal code provided");
      }, Collectors.collectingAndThen(
        Collectors.toList(),
        list -> list.stream()
          .map(arr -> Arrays.stream(arr)
            .filter(Objects::nonNull)
            .toArray(String[]::new))
          .toList())
    ));
  }

  public List<MemberAccess> getMemberAccessesByMemberId(UUID memberId) {
    return memberAccessJpaRepository.findByMemberId(memberId)
      .stream()
      .map(memberAccess -> MemberAccess.builder()
        .branchId(memberAccess.getGymBranchId())
        .direction(mapDirection(memberAccess.getDirection()))
        .timestamp(memberAccess.getCreatedAt())
        .build()
      )
      .toList();
  }

  private MemberAccess.Direction mapDirection(Integer direction) {
    return switch (direction) {
      case 0 -> MemberAccess.Direction.IN;
      case 1 -> MemberAccess.Direction.OUT;
      default -> throw new IllegalStateException("Unexpected value: " + direction);
    };
  }
}
