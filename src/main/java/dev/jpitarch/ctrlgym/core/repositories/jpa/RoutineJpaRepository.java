package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.RoutineDayEntity;
import dev.jpitarch.ctrlgym.core.entities.RoutineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutineJpaRepository extends JpaRepository<RoutineEntity, Integer> {

  List<RoutineEntity> findByMemberId(UUID memberId);

  List<RoutineEntity> findByGymIdAndMemberIdIsNull(Integer gymId);

  @Query("SELECT r FROM RoutineDayEntity r WHERE r.routine.id = :routineId AND r.dayNumber = :dayNumber")
  RoutineDayEntity findDay(Integer routineId, Short dayNumber);

}
