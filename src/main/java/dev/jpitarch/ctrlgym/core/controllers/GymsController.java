package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.services.RoutinesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/gyms")
@RequiredArgsConstructor
public class GymsController {

  private final RoutinesService routinesService;

  @PostMapping("/{gymId}/routines")
  public ResponseEntity<Routine> create(@PathVariable Integer gymId, @RequestBody MembersController.RoutineRequest request) {
    Routine routine = request.routine();
    routinesService.create(routine, gymId);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(value = "/{gymId}/routines")
  public Page<Routine> getRoutines(@PathVariable Integer gymId, Pageable pageable) {
    return routinesService.getRoutines(gymId, pageable);
  }

  @DeleteMapping("/{gymId}/routines/{routineId}")
  public ResponseEntity<Void> delete(@PathVariable Integer routineId) {
    routinesService.delete(routineId);
    return ResponseEntity.noContent().build();
  }

}
