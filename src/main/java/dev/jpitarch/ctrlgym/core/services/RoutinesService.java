package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.repositories.RoutinesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutinesService {

  private final RoutinesRepository routinesRepository;

  public Routine create(Routine routine, Member.Id memberId) {
    log.info("Creating a routine for member with id {}... ", memberId);
    return routinesRepository.save(routine, memberId);
  }

  public List<Routine> getRoutines(Member.Id memberId) {
    log.info("Retrieving routines for member  with id {}...", memberId);
    return routinesRepository.findByMemberId(memberId);
  }

  public void delete(Integer id, Member.Id memberId) {
    log.info("Deleting routine with id {} for member with id {}... ", id, memberId);
    routinesRepository.deleteById(id);
  }

}
