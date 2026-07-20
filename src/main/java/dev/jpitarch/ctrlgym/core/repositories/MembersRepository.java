package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.GymBranchId;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.enums.MemberStatus;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberNotFoundException;
import dev.jpitarch.ctrlgym.core.dto.MembersDistribution;
import dev.jpitarch.ctrlgym.core.models.MemberMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

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
      .gender(mapGender(memberMO.getGender()))
      .birthDate(memberMO.getBirthDate())
      .address(Member.Address.builder()
        .street(memberMO.getStreet())
        .city(memberMO.getCity())
        .state(memberMO.getState())
        .country(memberMO.getCountry())
        .postalCode(memberMO.getPostalCode())
        .build()
      )
      .build();
  }

  public void save(Member member, String customerId) {
    var memberMO = new MemberMO();
    memberMO.setId(member.getId().memberId());
    memberMO.setGymId(member.getId().gymId());
    memberMO.setName(member.getName());
    memberMO.setFirstSurname(member.getFirstSurname());
    memberMO.setSecondSurname(member.getSecondSurname());
    memberMO.setEmail(member.getEmail());
    memberMO.setGender(mapGender(member.getGender()));
    memberMO.setBirthDate(member.getBirthDate());
    memberMO.setStatus(MemberStatus.MEMBER);
    memberMO.setStripeCustomerId(customerId);

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
    return jpaRepository.getStripeCustomerId(memberId.memberId(), memberId.gymId());
  }

  public Optional<String> getPaymentMethodId(Member.Id id) {
    return jpaRepository.getStripePaymentMethodId(id.memberId(), id.gymId());
  }

  public Optional<String> getPaymentMethodId(String stripeCustomerId) {
    return jpaRepository.getStripePaymentMethodId(stripeCustomerId);
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
