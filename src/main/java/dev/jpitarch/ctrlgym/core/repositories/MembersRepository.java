package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberNotFoundException;
import dev.jpitarch.ctrlgym.core.entities.UserEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MembersRepository {

  private final UserJpaRepository jpaRepository;

  private final MemberAccessJpaRepository memberAccessJpaRepository;

  public boolean exists(UUID memberId) {
    return jpaRepository.existsById(memberId);
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

  public Member getById(UUID memberId) {
    UserEntity UserEntity = jpaRepository
      .findById(memberId)
      .orElseThrow(() -> new MemberNotFoundException(memberId));

    return Member.builder()
      .id(Member.Id.of(memberId))
      .nif(UserEntity.getNif())
      .email(UserEntity.getEmail())
      .name(UserEntity.getName())
      .firstSurname(UserEntity.getFirstSurname())
      .secondSurname(UserEntity.getSecondSurname())
      .gender(mapGender(UserEntity.getGender()))
      .birthDate(UserEntity.getBirthDate())
      .address(Member.Address.builder()
        .postalCode(UserEntity.getPostalCode())
        .build()
      )
      .status(UserEntity.getStatus())
      .build();
  }

  public String getRoleById(UUID memberId) {
    return jpaRepository
      .findRoleById(memberId)
      .orElseThrow(() -> new MemberNotFoundException(memberId));
  }

  public void save(Member member, String customerId) {
    var memberEntity = new UserEntity();
    memberEntity.setId(member.getId().memberId());
    memberEntity.setGymId(member.getGymId());
    memberEntity.setName(member.getName());
    memberEntity.setFirstSurname(member.getFirstSurname());
    memberEntity.setSecondSurname(member.getSecondSurname());
    memberEntity.setEmail(member.getEmail());
    memberEntity.setGender(mapGender(member.getGender()));
    memberEntity.setBirthDate(member.getBirthDate());
    memberEntity.setStatus(UserStatus.ACTIVE);
    memberEntity.setStripeCustomerId(customerId);

    if (member.getAddress() != null) {
      var address = member.getAddress();
      memberEntity.setPostalCode(address.getPostalCode());
    }

    jpaRepository.save(memberEntity);
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

  public List<MemberAccess> getMemberAccessesByMemberIdAndDateRange(UUID memberId, OffsetDateTime from, OffsetDateTime to) {
    return memberAccessJpaRepository.findByMemberIdAndDateRange(memberId, from, to)
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
