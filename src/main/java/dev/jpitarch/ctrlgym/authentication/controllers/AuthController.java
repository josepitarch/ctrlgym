package dev.jpitarch.ctrlgym.authentication.controllers;

import dev.jpitarch.ctrlgym.authentication.dtos.*;
import dev.jpitarch.ctrlgym.authentication.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

  private final SignupService signupService;

  private final LoginService loginService;

  private final RefreshTokenService refreshTokenService;

  private final InvitationService invitationService;

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signup(
    @RequestBody SignupRequest request,
    @RequestHeader(value = "X-Tenant-Id") Integer gymId) {
    AuthResponse response = signupService.signup(request, gymId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
    @RequestBody LoginRequest request,
    @RequestHeader(value = "X-Tenant-Id", required = false) Integer gymId) {
    AuthResponse response = loginService.login(request, gymId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
    AuthResponse response = refreshTokenService.refresh(request.refreshToken());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/invitations/accept")
  public ResponseEntity<AuthResponse> acceptInvitation(
    @RequestBody AcceptInvitationRequest request) {
    return ResponseEntity.ok(invitationService.acceptInvitation(request.token(), request.password()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
    refreshTokenService.logout(request.refreshToken());
    return ResponseEntity.ok().build();
  }
}
