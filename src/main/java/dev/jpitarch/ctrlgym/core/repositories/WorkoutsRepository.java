package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.domain.enums.WorkoutStatus;
import dev.jpitarch.ctrlgym.core.entities.RoutineDayEntity;
import dev.jpitarch.ctrlgym.core.entities.WorkoutEntity;
import dev.jpitarch.ctrlgym.core.mappers.WorkoutMapper;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.WorkoutJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WorkoutsRepository {

  private final WorkoutJpaRepository workoutJpaRepository;

  private final RoutineJpaRepository routineJpaRepository;

  private final WorkoutMapper workoutMapper;

  public Workout save(Workout workout, UUID memberId) {
    WorkoutEntity workoutEntity = workoutMapper.map(workout, memberId);

    if (workout.getRoutineId() != null && workout.getDayNumber() != null) {
      RoutineDayEntity routineDay = routineJpaRepository.findDay(workout.getRoutineId(), workout.getDayNumber().shortValue());
      workoutEntity.setRoutine(routineDay);
    }

    workoutEntity.getSets().forEach(set -> set.setWorkout(workoutEntity));

    WorkoutEntity saved = workoutJpaRepository.save(workoutEntity);
    return workoutMapper.map(saved);
  }

  public Page<Workout> findByMemberId(UUID memberId, Pageable pageable) {
    return workoutJpaRepository.findByMemberId(memberId, pageable).map(workoutMapper::map);
  }

  public long countByMemberIdAndStatus(UUID memberId, WorkoutStatus status) {
    return workoutJpaRepository.countByMemberIdAndStatus(memberId, status);
  }

  public Optional<Workout> findPreviousSession(UUID memberId, Integer currentId) {
    return workoutJpaRepository.findPreviousSession(memberId, currentId).map(workoutMapper::map);
  }

  public BigDecimal getAverageVolumeLast30Days(UUID memberId) {
    return workoutJpaRepository.getAverageVolumeLast30Days(memberId);
  }

}
