package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.MemberAccessMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MemberAccessJpaRepository extends JpaRepository<MemberAccessMO, Integer> {

  List<MemberAccessMO> findByMemberIdAndGymId(UUID memberId, Integer gymId);

  @Query("SELECT ma FROM MemberAccessMO ma WHERE ma.memberId = :memberId AND ma.gymId = :gymId AND ma.createdAt >= :from AND ma.createdAt <= :to")
  List<MemberAccessMO> findByMemberIdAndGymIdAndDateRange(UUID memberId, Integer gymId, OffsetDateTime from, OffsetDateTime to);
}
