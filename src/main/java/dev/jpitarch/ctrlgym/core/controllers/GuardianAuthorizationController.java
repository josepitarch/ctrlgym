package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.dto.GuardianAuthorizationDto;
import dev.jpitarch.ctrlgym.core.services.GuardianAuthorizationService;
import dev.jpitarch.ctrlgym.lib.RequestHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/guardian-authorization")
@RequiredArgsConstructor
public class GuardianAuthorizationController {

  private final GuardianAuthorizationService service;

  @GetMapping("/{token}")
  public GuardianAuthorizationDto getAuthorizationDetails(@PathVariable String token) {
    return service.getByToken(token);
  }

  @PostMapping("/{token}/approve")
  public ResponseEntity<Void> approve(@PathVariable String token,
                                      HttpServletRequest httpRequest) {
    service.approve(token, RequestHelper.extractIp(httpRequest), httpRequest.getHeader("User-Agent"));
    return ResponseEntity.noContent().build();
  }
}
