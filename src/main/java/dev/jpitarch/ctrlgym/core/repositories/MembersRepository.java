package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.enums.MemberStatus;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberNotFoundException;
import dev.jpitarch.ctrlgym.core.models.UserMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MembersRepository {

  private final UserJpaRepository jpaRepository;

  private final MemberAccessJpaRepository memberAccessJpaRepository;

  public boolean exists(Member.Id memberId) {
    return jpaRepository.existsById(new UserMO.ID(memberId.memberId(), memberId.gymId()));
  }

  public boolean exists(Integer gymId, String email) {
    return jpaRepository.existsByGymIdAndEmail(gymId, email);
  }

  public boolean existsAnotherGym(Integer gymId, String email) {
    return jpaRepository.existsByGymIdNotAndEmail(gymId, email);
  }

  public boolean isInMigration(Integer gymId, String email) {
    return jpaRepository.isInMigration(gymId, email);
  }

  public Member getById(Member.Id memberId) {
    var memberMOId = new UserMO.ID(memberId.memberId(), memberId.gymId());

    UserMO userMO = jpaRepository
      .findById(memberMOId)
      .orElseThrow(() -> new MemberNotFoundException(memberId));

    return Member.builder()
      .id(memberId)
      .nif(userMO.getNif())
      .email(userMO.getEmail())
      .name(userMO.getName())
      .firstSurname(userMO.getFirstSurname())
      .secondSurname(userMO.getSecondSurname())
      .gender(mapGender(userMO.getGender()))
      .birthDate(userMO.getBirthDate())
      .address(Member.Address.builder()
        .postalCode(userMO.getPostalCode())
        .build()
      )
      .status(userMO.getStatus())
      .build();
  }

  public String getRoleById(Member.Id memberId) {
    return jpaRepository
      .findRoleById(memberId.memberId(), memberId.gymId())
      .orElseThrow(() -> new MemberNotFoundException(memberId));
  }

  public void save(Member member, String customerId) {
    var memberMO = new UserMO();
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
      memberMO.setPostalCode(address.getPostalCode());
    }

    jpaRepository.save(memberMO);
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
      case null -> null;
      default -> throw new IllegalStateException("Unexpected value: " + gender);
    };
  }

}
