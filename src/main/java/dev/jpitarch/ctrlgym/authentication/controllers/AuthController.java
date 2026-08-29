package dev.jpitarch.ctrlgym.authentication.controllers;

import dev.jpitarch.ctrlgym.authentication.dtos.AuthResponse;
import dev.jpitarch.ctrlgym.authentication.dtos.RefreshRequest;
import dev.jpitarch.ctrlgym.authentication.dtos.SigninRequest;
import dev.jpitarch.ctrlgym.authentication.dtos.SignupRequest;
import dev.jpitarch.ctrlgym.authentication.services.AuthService;
import dev.jpitarch.ctrlgym.authentication.services.LoginService;
import dev.jpitarch.ctrlgym.authentication.services.RefreshTokenService;
import dev.jpitarch.ctrlgym.authentication.services.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

  private final SignupService signupService;

  private final LoginService loginService;

  private final RefreshTokenService refreshTokenService;

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
    AuthResponse response = signupService.signup(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody SigninRequest request) {
    AuthResponse response = loginService.login(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
    AuthResponse response = refreshTokenService.refresh(request.refreshToken());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
    refreshTokenService.logout(request.refreshToken());
    return ResponseEntity.ok().build();
  }
}
