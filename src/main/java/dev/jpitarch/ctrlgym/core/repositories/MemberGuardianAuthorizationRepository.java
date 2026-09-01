package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.MemberGuardianAuthorization;
import dev.jpitarch.ctrlgym.core.entities.MemberGuardianAuthorizationEntity;
import dev.jpitarch.ctrlgym.core.mappers.MemberGuardianAuthorizationMapper;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberGuardianAuthorizationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MemberGuardianAuthorizationRepository {

  private final MemberGuardianAuthorizationJpaRepository jpaRepository;

  private final MemberGuardianAuthorizationMapper mapper;

  private final MembersRepository membersRepository;

  public Optional<MemberGuardianAuthorization> findByToken(String token) {
    return jpaRepository.findByToken(token)
      .map(this::toDomainWithMember);
  }

  public Optional<MemberGuardianAuthorization> findByMemberId(UUID memberId) {
    return jpaRepository.findByMemberId(memberId)
      .map(this::toDomainWithMember);
  }

  public void save(MemberGuardianAuthorization domain) {
    MemberGuardianAuthorizationEntity entity = mapper.toEntity(domain);
    jpaRepository.save(entity);
  }

  private MemberGuardianAuthorization toDomainWithMember(MemberGuardianAuthorizationEntity entity) {
    MemberGuardianAuthorization domain = mapper.toDomain(entity);
    Member member = membersRepository.getById(entity.getMemberId());
    domain.setMember(member);
    return domain;
  }
}
