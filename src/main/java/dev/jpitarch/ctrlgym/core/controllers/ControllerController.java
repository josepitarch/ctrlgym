package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.dto.Heartbeat;
import dev.jpitarch.ctrlgym.core.entities.GymBranchHeartbeatEntity;
import dev.jpitarch.ctrlgym.core.entities.MemberAccessEntity;
import dev.jpitarch.ctrlgym.core.usecases.ControllerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/controllers")
public class ControllerController {

  private final ControllerUseCase controllerUseCase;

  @PostMapping("/{gymBranchId}/heartbeat")
  public ResponseEntity<Void> saveHeartbeat(@PathVariable Integer gymBranchId, @RequestBody GymBranchHeartbeatEntity heartbeat) {
    controllerUseCase.saveHeartbeat(gymBranchId, heartbeat);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{gymBranchId}/accesses")
  public ResponseEntity<Void> uploadAccessEvent(@PathVariable Integer gymBranchId, @RequestBody AccessesRequest accessesRequest) {
    controllerUseCase.uploadAccessEvent(gymBranchId, accessesRequest.accesses());
    return ResponseEntity.noContent().build();
  }
  
  @GetMapping("/{gymBranchId}/health")
  public ResponseEntity<Heartbeat> getHealth(@PathVariable Integer gymBranchId) {
    return ResponseEntity.ok(controllerUseCase.getHealth(gymBranchId));
  }

  public record AccessesRequest(List<MemberAccessEntity> accesses) {
  }

}
