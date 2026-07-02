package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.ExerciseMO;
import dev.jpitarch.ctrlgym.core.models.MemberAccessMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseJpaRepository extends JpaRepository<ExerciseMO, Integer> {

  List<ExerciseMO> findByGymId(Integer gymId);

}
