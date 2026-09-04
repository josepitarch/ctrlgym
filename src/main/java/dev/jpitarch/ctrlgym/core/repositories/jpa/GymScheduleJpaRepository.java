package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.GymScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GymScheduleJpaRepository extends JpaRepository<GymScheduleEntity, GymScheduleEntity.ID> {

  List<GymScheduleEntity> findByGymId(Integer gymId);

}
