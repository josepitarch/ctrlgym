package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.domain.Routine;
import dev.jpitarch.ctrlgym.core.services.RoutinesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/routines")
@RequiredArgsConstructor
public class RoutinesController {

  private final RoutinesService routinesService;

  @PostMapping
  public ResponseEntity<Routine> create(@RequestBody RoutineRequest request) {
    Routine routine = request.routine();
    UUID memberId = request.memberId();
    Integer gymId = request.gymId();

    Routine created = routinesService.create(routine, memberId, gymId);
    return ResponseEntity.ok(created);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Routine> update(@PathVariable Integer id, @RequestBody Routine routine) {
    routine.setId(id);
    Routine updated = routinesService.update(routine);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Integer id) {
    routinesService.delete(id);
    return ResponseEntity.noContent().build();
  }

  public record RoutineRequest(Routine routine, UUID memberId, Integer gymId) {}

}