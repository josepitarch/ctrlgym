package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.RoutineDayMO;
import dev.jpitarch.ctrlgym.core.models.RoutineMO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutineJpaRepository extends JpaRepository<RoutineMO, Integer> {
  List<RoutineMO> findByMemberIdAndGymId(UUID memberId, Integer gymId);

  @Query("SELECT r FROM RoutineDayMO r WHERE r.routine.id = :routineId AND r.dayNumber = :dayNumber")
  RoutineDayMO findDay(Integer routineId, Short dayNumber);

}
