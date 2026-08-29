package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.*;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.core.dto.OccupancyGranularity;
import dev.jpitarch.ctrlgym.core.entities.GymEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GymsRepository {

  private final GymJpaRepository jpaRepository;

  private final NamedParameterJdbcTemplate jdbc;

  public GymEntity getById(Integer gymId) {
    return jpaRepository.findById(gymId).orElseThrow();
  }

  public String getVerifactuApiKey(Integer gymId) {
    return jpaRepository.findVerifactiApiKeyById(gymId);
  }

  public String getControllerApiKey(Integer gymId) {
    return jpaRepository.findControllerApiKey(gymId);
  }

  public Integer getId(String stripeAccountId) {
    return jpaRepository.findIdByStripeAccountId(stripeAccountId);
  }

  public List<GymBranch> getBranches(Integer gymId) {
    GymEntity GymEntity = jpaRepository.findById(gymId).orElseThrow();

    return GymEntity.getBranches().stream().map(branchEntity -> GymBranch.builder()
        .id(branchEntity.getId())
        .name(branchEntity.getName())
        .capacity(branchEntity.getCapacity())
        .peakHour(new GymBranch.PeakHour(branchEntity.getPeakHourStart(), branchEntity.getPeakHourEnd()))
        .coordinates(new GymBranch.Coordinates(branchEntity.getLatitude(), branchEntity.getLongitude()))
        .build())
      .toList();
  }

  public GymBranch getGymBranch(GymBranchId gymBranchId) {
    var branchEntity = jpaRepository.findBranchByGymIdAndBranchId(gymBranchId.gymId(), gymBranchId.branchId());

    return GymBranch.builder()
      .id(branchEntity.getId())
      .name(branchEntity.getName())
      .capacity(branchEntity.getCapacity())
      .peakHour(new GymBranch.PeakHour(branchEntity.getPeakHourStart(), branchEntity.getPeakHourEnd()))
      .coordinates(new GymBranch.Coordinates(branchEntity.getLatitude(), branchEntity.getLongitude()))
      .build();
  }

  public List<Member> getMembers(GymBranchId gymBranchId, String q) {
    var sql = """
      WITH ranked_memberships AS (
        SELECT
          mb.member_id,
          mb.start_date,
          mb.end_date,
          ROW_NUMBER() OVER (
            PARTITION BY mb.member_id
            ORDER BY
              CASE WHEN mb.start_date <= CURRENT_DATE AND (mb.end_date IS NULL OR mb.end_date >= CURRENT_DATE) THEN 0 ELSE 1 END,
              mb.start_date DESC
          ) AS rn
        FROM memberships mb
        JOIN membership_plans mp ON mb.membership_plan_id = mp.id
        WHERE mp.gym_branch_id = :gymBranchId
      )
      SELECT m.id, m.name, m.first_surname, m.second_surname, m.avatar_url, m.nif, m.email, m.gender, m.birth_date, m.gym_id,
      m.postal_code,
      CASE
        WHEN rm.start_date <= CURRENT_DATE AND (rm.end_date IS NULL OR rm.end_date >= CURRENT_DATE)
        THEN true
        ELSE false
      END AS is_active
      FROM users m
      JOIN ranked_memberships rm ON m.id = rm.member_id AND rm.rn = 1
      WHERE m.gym_id = :gymId
      """;

    var params = new HashMap<String, Object>();
    params.put("gymId", gymBranchId.gymId());
    params.put("gymBranchId", gymBranchId.branchId());

    if (q != null) {
      if (Character.isDigit(q.charAt(0))) {
        sql += " AND m.nif LIKE :q";
      } else {
        sql += " AND (LOWER(m.name) LIKE LOWER(:q) OR LOWER(m.first_surname) LIKE LOWER(:q) OR LOWER(m.second_surname) LIKE LOWER(:q) OR LOWER(m.email) LIKE LOWER(:q))";
      }
      params.put("q", "%" + q + "%");
    }

    return jdbc.query(sql, params, (rs, _) -> Member.builder()
        .id(User.Id.of(UUID.fromString(rs.getString("id")), rs.getInt("gym_id")))
        .avatarUrl(Optional.ofNullable(rs.getString("avatar_url")).map(URI::create).orElse(null))
        .name(rs.getString("name"))
        .nif(rs.getString("nif"))
        .firstSurname(rs.getString("first_surname"))
        .secondSurname(rs.getString("second_surname"))
        .email(rs.getString("email"))
        .gender(mapGender(rs.getString("gender")))
        .birthDate(LocalDate.parse(rs.getString("birth_date")))
        .isActive(rs.getBoolean("is_active"))
        .address(Member.Address.builder()
          .postalCode(rs.getObject("postal_code", Integer.class))
          .build())
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
