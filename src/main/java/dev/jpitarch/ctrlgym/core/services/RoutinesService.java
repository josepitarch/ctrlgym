package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.repositories.RoutinesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoutinesService {

  private final RoutinesRepository routinesRepository;

  public Routine create(Routine routine, UUID memberId) {
    return create(routine, memberId, null);
  }

  public Routine create(Routine routine, Integer gymId) {
    return create(routine, null, gymId);
  }

  private Routine create(Routine routine, UUID memberId, Integer gymId) {
    return routinesRepository.save(routine, memberId, gymId);
  }

  public Page<Routine> getRoutines(UUID memberId, Pageable pageable) {
    return routinesRepository.findByMemberId(memberId, pageable);
  }

  public Page<Routine> getRoutines(Integer gymId, Pageable pageable) {
    //TODO
    return null;
  }


  public Routine update(Routine routine) {
    return routinesRepository.save(routine, null, null);
  }

  public void delete(Integer id) {
    routinesRepository.deleteById(id);
  }

}
