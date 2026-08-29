package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.MemberAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MemberAccessJpaRepository extends JpaRepository<MemberAccessEntity, Integer> {

  List<MemberAccessEntity> findByMemberIdAndGymId(UUID memberId, Integer gymId);

  @Query("SELECT ma FROM MemberAccessEntity ma WHERE ma.memberId = :memberId AND ma.gymId = :gymId AND ma.createdAt >= :from AND ma.createdAt <= :to")
  List<MemberAccessEntity> findByMemberIdAndGymIdAndDateRange(UUID memberId, Integer gymId, OffsetDateTime from, OffsetDateTime to);
}
