package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.repositories.ExercisesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExercisesService {

  private final ExercisesRepository exercisesRepository;

  public List<Exercise> getAll(Integer gymId) {
    return exercisesRepository.getAll(gymId);
  }

}
