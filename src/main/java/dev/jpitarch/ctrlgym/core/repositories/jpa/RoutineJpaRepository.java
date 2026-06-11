package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.RoutineMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutineJpaRepository extends JpaRepository<RoutineMO, Integer> {

  List<RoutineMO> findByMemberId(UUID memberId);

  List<RoutineMO> findByGymId(Integer gymId);

  List<RoutineMO> findByMemberIdOrGymId(UUID memberId, Integer gymId);

}