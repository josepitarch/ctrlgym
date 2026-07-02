package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.services.ExercisesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/gyms")
public class GymController {

  private final ExercisesService exercisesService;

  @GetMapping("/{gymId}/exercises")
  public ResponseEntity<List<Exercise>> getExercises(@PathVariable Integer gymId) {
    return ResponseEntity.ok(exercisesService.getAll(gymId));
  }

}
