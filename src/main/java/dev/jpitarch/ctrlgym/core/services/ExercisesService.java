package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.repositories.ExercisesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExercisesService {

  private final ExercisesRepository exercisesRepository;

  public List<Exercise> getAll(Integer gymId) {
    log.info("Retrieving exercises of gym with id {}...", gymId);
    return exercisesRepository.getAll(gymId);
  }

  public Exercise create(Exercise exercise, Integer gymId) {
    log.info("Creating exercise {} for gym with id {}...", exercise.getName(), gymId);
    return exercisesRepository.create(exercise, gymId);
  }

  public void delete(Integer exerciseId, Integer gymId) {
    log.info("Deleting exercise with id {} for gym with id {}...", exerciseId, gymId);
    exercisesRepository.delete(exerciseId);
  }

}
