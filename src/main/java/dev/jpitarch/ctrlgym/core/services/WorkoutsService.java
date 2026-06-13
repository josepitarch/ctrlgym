package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Workout;
import dev.jpitarch.ctrlgym.core.repositories.WorkoutsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutsService {

  private final WorkoutsRepository workoutsRepository;

  public void create(Workout workout, UUID memberId) {
    log.info("Creating workout for member {}...", memberId);
    workoutsRepository.save(workout, memberId);
  }

  public Page<Workout> getWorkouts(UUID memberId, Pageable pageable) {
    return workoutsRepository.findByMemberId(memberId, pageable);
  }

}
