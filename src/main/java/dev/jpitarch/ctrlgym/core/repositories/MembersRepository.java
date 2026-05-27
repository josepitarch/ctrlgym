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

  public List<Member> getMembers(GymBranchId gymBranchId) {
    var sql = """
      SELECT m.id, m.name, m.first_surname, m.second_surname, m.email, m.gender, m.birth_date, m.postal_code
      FROM members m
      JOIN memberships ms ON m.id = ms.member_id
      JOIN membership_plan_branches mpb ON ms.membership_plan_id = mpb.membership_plan_id
      WHERE m.gym_id = :gymId AND mpb.branch_id = :gymBranchId
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId(),
      "gymBranchId", gymBranchId.branchId()
    );

    return jdbc.query(sql, params, (rs, _) -> {
      var id = rs.getString("id");
      var name = rs.getString("name");
      var firstSurname = rs.getString("first_surname");
      var secondSurname = rs.getString("second_surname");
      var email = rs.getString("email");
      var gender = rs.getString("gender");
      var birthDate = rs.getDate("birth_date").toLocalDate();
      var postalCode = rs.getInt("postal_code");
      return new Member(UUID.fromString(id), name, firstSurname, secondSurname, email, Gender.fromCode(gender), birthDate, postalCode);
    });
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
    return switch(direction) {
      case 0 -> MemberAccess.Direction.IN;
      case 1 -> MemberAccess.Direction.OUT;
      default -> throw new IllegalStateException("Unexpected value: " + direction);
    };
  }
}
