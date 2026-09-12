package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberAccess;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.domain.exceptions.MemberNotFoundException;
import dev.jpitarch.ctrlgym.core.entities.UserEntity;
import dev.jpitarch.ctrlgym.core.mappers.MemberMapper;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberAccessJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MembersRepository {

  private final UserJpaRepository jpaRepository;

  private final MemberAccessJpaRepository memberAccessJpaRepository;

  private final MemberMapper memberMapper;

  public boolean exists(UUID memberId) {
    return jpaRepository.existsById(memberId);
  }

  public boolean exists(Integer gymId, String email) {
    return jpaRepository.existsByGymIdAndEmail(gymId, email);
  }

  public Member getById(UUID memberId) {
    UserEntity UserEntity = jpaRepository
      .findById(memberId)
      .orElseThrow(() -> new MemberNotFoundException(memberId));

    return memberMapper.toDomain(UserEntity);
  }

  public Integer getGymIdByMemberId(UUID memberId) {
    return jpaRepository.findById(memberId)
      .map(UserEntity::getGymId)
      .orElseThrow(() -> new MemberNotFoundException(memberId));
  }

  public String getRoleById(UUID memberId) {
    return jpaRepository
      .findRoleById(memberId)
      .orElseThrow(() -> new MemberNotFoundException(memberId));
  }

  public void updateStatus(UUID memberId, UserStatus status) {
    var memberEntity = jpaRepository.findById(memberId)
      .orElseThrow(() -> new MemberNotFoundException(memberId));
    memberEntity.setStatus(status);
    jpaRepository.save(memberEntity);
  }

  public void updatePostalCode(UUID memberId, Integer postalCode) {
    var memberEntity = jpaRepository.findById(memberId)
      .orElseThrow(() -> new MemberNotFoundException(memberId));
    memberEntity.setPostalCode(postalCode);
    jpaRepository.save(memberEntity);
  }

  public String getAvatarUrl(UUID memberId) {
    return jpaRepository.findById(memberId)
      .map(UserEntity::getAvatarUrl)
      .orElse(null);
  }

  public void updateAvatarUrl(UUID memberId, String avatarUrl) {
    var memberEntity = jpaRepository.findById(memberId)
      .orElseThrow(() -> new MemberNotFoundException(memberId));
    memberEntity.setAvatarUrl(avatarUrl);
    jpaRepository.save(memberEntity);
  }

  public List<MemberAccess> getMemberAccessesByMemberId(UUID memberId) {
    return memberAccessJpaRepository.findByMemberId(memberId)
      .stream()
      .map(memberMapper::toDomain)
      .toList();
  }

  public List<MemberAccess> getMemberAccessesByMemberIdAndDateRange(UUID memberId, OffsetDateTime from, OffsetDateTime to) {
    return memberAccessJpaRepository.findByMemberIdAndDateRange(memberId, from, to)
      .stream()
      .map(memberMapper::toDomain)
      .toList();
  }

}
