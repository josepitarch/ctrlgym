package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.MemberPersonalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemberPersonalRecordJpaRepository extends JpaRepository<MemberPersonalRecordEntity, UUID> {

  List<MemberPersonalRecordEntity> findByMemberIdAndExerciseIdOrderByAchievedAtDesc(UUID memberId, Integer exerciseId);

  List<MemberPersonalRecordEntity> findByMemberIdOrderByAchievedAtDesc(UUID memberId);
}
