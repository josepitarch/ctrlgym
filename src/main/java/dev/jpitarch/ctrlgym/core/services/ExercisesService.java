package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.domain.exceptions.ExerciseNotFoundException;
import dev.jpitarch.ctrlgym.core.repositories.ExercisesRepository;
import dev.jpitarch.ctrlgym.storage.config.StorageBucket;
import dev.jpitarch.ctrlgym.storage.services.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExercisesService {

  private final ExercisesRepository exercisesRepository;

  private final StorageService storageService;

  public List<Exercise> getAll(Integer gymId) {
    log.info("Retrieving exercises of gym with id {}...", gymId);
    List<Exercise> exercises = exercisesRepository.getAll(gymId);
    exercises.forEach(this::resolveExerciseImageUrl);
    return exercises;
  }

  public Exercise create(Exercise exercise, Integer gymId, MultipartFile image) {
    log.info("Creating exercise {} for gym with id {}...", exercise.getName(), gymId);
    if (image != null && !image.isEmpty()) {
      String imageKey = storageService.uploadFile(image, gymId, "exercises", StorageBucket.ASSETS);
      exercise.setImage(imageKey);
    }
    Exercise created = exercisesRepository.create(exercise, gymId);
    resolveExerciseImageUrl(created);
    return created;
  }

  public Optional<Exercise> findById(Integer exerciseId) {
    return exercisesRepository.findById(exerciseId);
  }

  public Exercise update(Integer exerciseId, Integer gymId, Exercise exercise, MultipartFile image) {
    log.info("Updating exercise with id {}...", exerciseId);
    Exercise existing = exercisesRepository.findById(exerciseId)
      .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

    if (image != null && !image.isEmpty()) {
      if (existing.getImage() != null && !existing.getImage().isBlank()) {
        storageService.deleteFile(existing.getImage(), StorageBucket.ASSETS);
      }
      String imageKey = storageService.uploadFile(image, gymId, "exercises", StorageBucket.ASSETS);
      exercise.setImage(imageKey);
    } else {
      exercise.setImage(existing.getImage());
    }

    exercise.setId(exerciseId);
    Exercise updated = exercisesRepository.update(exercise);
    resolveExerciseImageUrl(updated);
    return updated;
  }

  public void delete(Integer exerciseId, Integer gymId) {
    log.info("Deleting exercise with id {} for gym with id {}...", exerciseId, gymId);
    Exercise exercise = exercisesRepository.findById(exerciseId)
      .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

    if (exercise.getImage() != null && !exercise.getImage().isBlank()) {
      storageService.deleteFile(exercise.getImage(), StorageBucket.ASSETS);
    }
    exercisesRepository.delete(exerciseId);
  }

  private void resolveExerciseImageUrl(Exercise exercise) {
    if (exercise.getImage() != null && !exercise.getImage().isBlank()) {
      exercise.setImage(storageService.resolvePublicUrl(exercise.getImage()));
    }
  }

}
