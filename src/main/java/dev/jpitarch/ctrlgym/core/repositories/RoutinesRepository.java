package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.mappers.RoutineMapper;
import dev.jpitarch.ctrlgym.core.entities.*;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExerciseJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RoutinesRepository {

  private final RoutineJpaRepository routineJpaRepository;

  private final ExerciseJpaRepository exerciseJpaRepository;

  private final RoutineMapper mapper;

  public Routine save(Routine routine, UUID memberId, Integer gymId) {
    RoutineEntity routineEntity = mapper.map(routine, memberId, gymId);
    routineEntity.setCreatedAt(Instant.now());
    RoutineEntity saved = routineJpaRepository.save(routineEntity);
    List<ExerciseEntity> exercises = exerciseJpaRepository.findAll();
    return mapper.mapWithContext(saved, exercises);
  }

  public void deleteById(Integer id) {
    routineJpaRepository.deleteById(id);
  }

  public List<Routine> findByMemberId(UUID memberId) {
    List<ExerciseEntity> exercises = exerciseJpaRepository.findAll();
    return routineJpaRepository
      .findByMemberId(memberId)
      .stream()
      .map(r -> mapper.mapWithContext(r, exercises))
      .toList();
  }

  public Routine saveForGym(Routine routine, Integer gymId) {
    RoutineEntity routineEntity = mapper.map(routine, null, gymId);
    routineEntity.setCreatedAt(Instant.now());
    RoutineEntity saved = routineJpaRepository.save(routineEntity);
    List<ExerciseEntity> exercises = exerciseJpaRepository.findAll();
    return mapper.mapWithContext(saved, exercises);
  }

  public List<Routine> findByGymId(Integer gymId) {
    List<ExerciseEntity> exercises = exerciseJpaRepository.findAll();
    return routineJpaRepository
      .findByGymIdAndMemberIdIsNull(gymId)
      .stream()
      .map(r -> mapper.mapWithContext(r, exercises))
      .toList();
  }

}
