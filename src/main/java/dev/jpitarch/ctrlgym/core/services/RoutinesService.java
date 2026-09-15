package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.repositories.RoutinesRepository;
import dev.jpitarch.ctrlgym.storage.services.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutinesService {

  private final RoutinesRepository routinesRepository;
  private final StorageService storageService;

  public Routine create(Routine routine, UUID memberId, Integer gymId) {
    log.info("Creating a routine for member with id {}... ", memberId);
    return routinesRepository.save(routine, memberId, gymId);
  }

  public List<Routine> getRoutines(UUID memberId) {
    log.debug("Retrieving routines for member with id {}...", memberId);
    List<Routine> routines = routinesRepository.findByMemberId(memberId);
    routines.forEach(this::resolveRoutineExerciseImageUrls);
    return routines;
  }

  public void delete(Integer id, UUID memberId) {
    log.info("Deleting routine with id {} for member with id {}... ", id, memberId);
    routinesRepository.deleteById(id);
  }

  public Routine createForGym(Routine routine, Integer gymId) {
    log.info("Creating a routine for gym with id {}... ", gymId);
    return routinesRepository.saveForGym(routine, gymId);
  }

  public List<Routine> getGymRoutines(Integer gymId) {
    log.info("Retrieving routines for gym with id {}...", gymId);
    List<Routine> routines = routinesRepository.findByGymId(gymId);
    routines.forEach(this::resolveRoutineExerciseImageUrls);
    return routines;
  }

  public void deleteForGym(Integer id, Integer gymId) {
    log.info("Deleting routine with id {} for gym with id {}... ", id, gymId);
    routinesRepository.deleteById(id);
  }

  private void resolveRoutineExerciseImageUrls(Routine routine) {
    if (routine.getDays() == null) return;
    routine.getDays().forEach(day -> {
      if (day.getExercises() == null) return;
      day.getExercises().forEach(exercise -> {
        if (exercise.getImage() != null && !exercise.getImage().isBlank()) {
          exercise.setImage(storageService.resolvePublicUrl(exercise.getImage()));
        }
      });
    });
  }

}
