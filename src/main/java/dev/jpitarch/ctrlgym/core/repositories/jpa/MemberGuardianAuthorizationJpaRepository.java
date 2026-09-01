package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.MemberGuardianAuthorizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberGuardianAuthorizationJpaRepository extends JpaRepository<MemberGuardianAuthorizationEntity, UUID> {

  Optional<MemberGuardianAuthorizationEntity> findByToken(String token);

  Optional<MemberGuardianAuthorizationEntity> findByMemberId(UUID memberId);
}
