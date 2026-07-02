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
    log.info("Retrieving exercises of gym with id {}", gymId);
    return exercisesRepository.getAll(gymId);
  }

}
