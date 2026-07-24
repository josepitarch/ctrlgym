package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.DatePeriod;
import dev.jpitarch.ctrlgym.core.domain.GymBranch;
import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.core.dto.OccupancyGranularity;
import dev.jpitarch.ctrlgym.core.models.GymMO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GymsRepository {

  private final GymRepositoryJpaRepository jpaRepository;

  private final NamedParameterJdbcTemplate jdbc;

  public String getVerifactuApiKey(Integer gymId) {
    return jpaRepository.findVerifactiApiKeyById(gymId);
  }

  public String getControllerApiKey(Integer gymId) {
    return jpaRepository.findControllerApiKey(gymId);
  }

  public String getStripeAccountId(Integer gymId) {
    return jpaRepository.findStripeAccountIdById(gymId);
  }

  public Integer getId(String stripeAccountId) {
    return jpaRepository.findIdByStripeAccountId(stripeAccountId);
  }

  public List<GymBranch> getBranches(Integer gymId) {
    GymMO gymMO = jpaRepository.findById(gymId).orElseThrow();

    return gymMO.getBranches().stream().map(branchMO -> GymBranch.builder()
        .id(branchMO.getId())
        .name(branchMO.getName())
        .capacity(branchMO.getCapacity())
        .peakHour(new GymBranch.PeakHour(branchMO.getPeakHourStart(), branchMO.getPeakHourEnd()))
        .coordinates(new GymBranch.Coordinates(branchMO.getLatitude(), branchMO.getLongitude()))
        .build())
      .toList();
  }

  public GymBranch getGymBranch(GymBranchId gymBranchId) {
    var sql = """
        SELECT b.id, b.name, b.capacity, b.peak_hour_start, b.peak_hour_end
        FROM gym_branches b
        WHERE b.gym_id = :gymId AND b.id = :branchId
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId(),
      "branchId", gymBranchId.branchId()
    );

    return this.jdbc.queryForObject(sql, params, (rs, rowNum) ->
      GymBranch.builder()
        .id(rs.getShort("id"))
        .name(rs.getString("name"))
        .capacity(rs.getShort("capacity"))
        .peakHour(new GymBranch.PeakHour(
          rs.getTime("peak_hour_start").toLocalTime(),
          rs.getTime("peak_hour_end").toLocalTime()
        ))
        .build()
    );
  }

  public List<Member> getMembers(GymBranchId gymBranchId) {
    var sql = """
      SELECT m.id, m.name, m.first_surname, m.second_surname, m.avatar_url, m.nif, m.email, m.gender, m.birth_date, m.street, m.state, m.city, m.postal_code, m.country, m.gym_id
      FROM members m
      JOIN memberships mb ON m.id = mb.member_id
      JOIN membership_plans mp ON mb.membership_plan_id = mp.id
      WHERE m.gym_id = :gymId AND mp.gym_branch_id = :gymBranchId
      """;

    var params = Map.of(
      "gymId", gymBranchId.gymId(),
      "gymBranchId", gymBranchId.branchId()
    );

    return jdbc.query(sql, params, (rs, rowNum) -> Member.builder()
      .id(Member.Id.of(UUID.fromString(rs.getString("id")), rs.getInt("gym_id")))
      .avatarUrl(Optional.ofNullable(rs.getString("avatar_url")).map(URI::create).orElse(null))
      .name(rs.getString("name"))
      .nif(rs.getString("nif"))
      .firstSurname(rs.getString("first_surname"))
      .secondSurname(rs.getString("second_surname"))
      .email(rs.getString("email"))
      .gender(mapGender(rs.getString("gender")))
      .birthDate(LocalDate.parse(rs.getString("birth_date")))
      .address(new Member.Address(rs.getString("street"), rs.getString("city"), rs.getString("state"), rs.getInt("postal_code"), rs.getString("country")))
      .build()
    );
  }


  public Short getCurrentOccupancy(GymBranchId gymBranchId) {
    var sql = """
          SELECT count
          FROM gym_branch_current_occupancy
          WHERE gym_branch_id = :gymBranchId
      """;

    var params = Map.of("gymBranchId", gymBranchId.branchId());

    return this.jdbc.queryForObject(sql, params, Short.class);
  }

  public List<OccupancyGranularity.OccupancyDataPoint> getOccupancies(GymBranchId gymBranchId, DatePeriod datePeriod, Granularity granularity) {
    var sql = """
      SELECT
      DATE_TRUNC(:granularity, snapshot_time) AS bucket,
      AVG(count) AS avg_occupancy
      FROM gym_branch_occupancy_snapshot
      WHERE gym_branch_id = :gymBranchId
      AND snapshot_time BETWEEN :from AND :to
      GROUP BY 1
      ORDER BY 1;
      """;

    var params = Map.of(
      "gymBranchId", gymBranchId.branchId(),
      "from", datePeriod.from(),
      "to", datePeriod.to(),
      "granularity", convertGranularity(granularity)
    );

    return jdbc.query(sql, params, (row, _) -> {
      var bucket = row.getTimestamp("bucket").toLocalDateTime();
      var avgOccupancy = row.getInt("avg_occupancy");
      return new OccupancyGranularity.OccupancyDataPoint(bucket, avgOccupancy);
    });
  }

  private String convertGranularity(Granularity granularity) {
    return switch (granularity) {
      case HOURS -> "hour";
      case DAILY -> "day";
      case WEEKLY -> "week";
      case MONTHLY -> "month";
    };
  }

  private String mapGender(Gender gender) {
    return switch (gender) {
      case MALE -> "M";
      case FEMALE -> "F";
    };
  }

  private Gender mapGender(String gender) {
    return switch (gender) {
      case "M" -> Gender.MALE;
      case "F" -> Gender.FEMALE;
      default -> throw new IllegalStateException("Unexpected value: " + gender);
    };
  }

}
