package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.MemberTermsAcceptanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberTermsAcceptanceJpaRepository extends JpaRepository<MemberTermsAcceptanceEntity, UUID> {

  @Query("SELECT m FROM MemberTermsAcceptanceEntity m WHERE m.memberId = :memberId AND m.revokedAt IS NULL")
  List<MemberTermsAcceptanceEntity> findActiveByMemberId(UUID memberId);

  @Query("SELECT m FROM MemberTermsAcceptanceEntity m WHERE m.memberId = :memberId AND m.documentVersionId = :documentVersionId AND m.revokedAt IS NULL")
  Optional<MemberTermsAcceptanceEntity> findActiveByMemberIdAndDocumentVersionId(UUID memberId, UUID documentVersionId);

  @Query("SELECT COUNT(m) FROM MemberTermsAcceptanceEntity m WHERE m.documentVersionId = :documentVersionId AND m.revokedAt IS NULL")
  long countActiveByDocumentVersionId(UUID documentVersionId);
}
