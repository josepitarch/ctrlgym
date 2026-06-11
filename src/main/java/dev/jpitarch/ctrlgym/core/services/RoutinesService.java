package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.repositories.RoutinesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoutinesService {

  private final RoutinesRepository routinesRepository;

  public Routine create(Routine routine, UUID memberId, Integer gymId) {
    if (memberId != null && gymId != null) {
      throw new IllegalArgumentException("memberId and gymId cannot be both null");
    }

    return routinesRepository.save(routine, memberId, gymId);
  }

  public Routine update(Routine routine) {
    return routinesRepository.save(routine, null, null);
  }

  public void delete(Integer id) {
    routinesRepository.deleteById(id);
  }

}
