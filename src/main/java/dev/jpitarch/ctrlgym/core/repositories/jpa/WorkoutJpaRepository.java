package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.domain.enums.WorkoutStatus;
import dev.jpitarch.ctrlgym.core.entities.WorkoutEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutJpaRepository extends JpaRepository<WorkoutEntity, Integer> {

  Page<WorkoutEntity> findByMemberId(UUID memberId, Pageable pageable);

  List<WorkoutEntity> findByMemberId(UUID memberId);

  long countByMemberIdAndStatus(UUID memberId, WorkoutStatus status);

  @Query("""
      SELECT w FROM WorkoutEntity w
      WHERE w.memberId = :memberId AND w.id <> :currentId AND w.status = dev.jpitarch.ctrlgym.core.domain.enums.WorkoutStatus.COMPLETED
      ORDER BY w.finishedAt DESC
      LIMIT 1
      """)
  Optional<WorkoutEntity> findPreviousSession(UUID memberId, Integer currentId);

  Optional<WorkoutEntity> findFirstByMemberIdAndStatusOrderByFinishedAtDesc(UUID memberId, WorkoutStatus status);

  @Query(value = """
      SELECT AVG(total_volume)
      FROM (
          SELECT ws.id, SUM(wset.weight * wset.reps) AS total_volume
          FROM workouts ws
          JOIN workout_sets wset ON wset.workout_id = ws.id
          WHERE ws.member_id = :memberId
            AND ws.status = 'COMPLETED'
            AND ws.finished_at >= NOW() - INTERVAL '30 days'
          GROUP BY ws.id
      ) sub
      """, nativeQuery = true)
  BigDecimal getAverageVolumeLast30Days(UUID memberId);

}