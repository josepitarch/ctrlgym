package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.enums.MemberDistribution;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberNotFoundException;
import dev.jpitarch.ctrlgym.core.models.MemberMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MembersRepository {

  private final MemberJpaRepository jpaRepository;

  private final NamedParameterJdbcTemplate jdbc;

  private final MemberAccessJpaRepository memberAccessJpaRepository;

  public boolean exists(Member.Id memberId) {
    return jpaRepository.existsById(new MemberMO.ID(memberId.memberId(), memberId.gymId()));
  }

  public Member getById(Member.Id memberId) {
    var memberMOId = new MemberMO.ID(memberId.memberId(), memberId.gymId());

    MemberMO memberMO = jpaRepository
      .findById(memberMOId)
      .orElseThrow(() -> new MemberNotFoundException(memberId));

    return Member.builder()
      .id(memberId)
      .nif(memberMO.getNif())
      .email(memberMO.getEmail())
      .name(memberMO.getName())
      .firstSurname(memberMO.getFirstSurname())
      .secondSurname(memberMO.getSecondSurname())
      .gender(Gender.fromCode(memberMO.getGender()))
      .birthDate(memberMO.getBirthDate())
      .address(Member.Address.builder().postalCode(memberMO.getPostalCode()).build())
      .build();
  }

  public void save(Member member) {
    MemberMO memberMO = new MemberMO();
    memberMO.setId(member.getId().memberId());
    memberMO.setGymId(member.getId().gymId());
    memberMO.setName(member.getName());
    memberMO.setFirstSurname(member.getFirstSurname());
    memberMO.setSecondSurname(member.getSecondSurname());
    memberMO.setEmail(member.getEmail());
    memberMO.setGender(member.getGender().name());
    memberMO.setBirthDate(member.getBirthDate());

    if (member.getAddress() != null) {
      var address = member.getAddress();
      memberMO.setStreet(address.getStreet());
      memberMO.setCity(address.getCity());
      memberMO.setState(address.getState());
      memberMO.setPostalCode(address.getPostalCode());
      memberMO.setCountry(address.getCountry());
    }

    jpaRepository.save(memberMO);
  }

  public Optional<String> getStripeCustomerId(Member.Id memberId) {
    var sql = """
      SELECT stripe_customer_id
      FROM members
      WHERE id = :id  AND gym_id = :gymId
      """;

    var params = Map.of(
      "id", memberId.memberId(),
      "gymId", memberId.gymId()
    );

    return Optional.ofNullable(this.jdbc.queryForObject(sql, params, String.class));
  }

  public Member.Id getId(String stripeCustomerId) {
    var sql = """
        SELECT id, gym_id
        FROM members
        WHERE stripe_customer_id = :stripeCustomerId
      """;

    var params = Map.of("stripeCustomerId", stripeCustomerId);

    return this.jdbc.queryForObject(sql, params, (rs, _) -> Member.Id.of(
      UUID.fromString(rs.getString("id")),
      rs.getInt("gym_id"))
    );

  }

  public Optional<String> getPaymentMethodId(Member.Id id) {
    var sql = """
      SELECT stripe_payment_method_id
      FROM members
      WHERE id = :id AND gym_id = :gymId
      """;

    var params = Map.of(
      "id", id,
      "gymId", id.gymId()
    );

    return Optional.ofNullable(this.jdbc.queryForObject(sql, params, String.class));
  }

  public void saveCustomerId(Member.Id memberId, String customerId) {
    MemberMO member = jpaRepository.getReferenceById(new MemberMO.ID(memberId.memberId(), memberId.gymId()));
    member.setStripeCustomerId(customerId);
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

    return jdbc.query(sql, (rs, _) -> new String[]{
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

  public List<MemberAccess> getMemberAccessesByMemberId(Member.Id memberId) {
    return memberAccessJpaRepository.findByMemberIdAndGymId(memberId.memberId(), memberId.gymId())
      .stream()
      .map(memberAccess -> MemberAccess.builder()
        .branchId(memberAccess.getGymBranchId())
        .direction(mapDirection(memberAccess.getDirection()))
        .timestamp(memberAccess.getCreatedAt())
        .build()
      )
      .toList();
  }

  public List<MemberAccess> getMemberAccessesByMemberIdAndDateRange(Member.Id memberId, OffsetDateTime from, OffsetDateTime to) {
    return memberAccessJpaRepository.findByMemberIdAndGymIdAndDateRange(memberId.memberId(), memberId.gymId(), from, to)
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
