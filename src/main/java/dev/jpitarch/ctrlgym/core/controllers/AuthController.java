package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.dto.SigninRequest;
import dev.jpitarch.ctrlgym.core.dto.SigninResponse;
import dev.jpitarch.ctrlgym.core.dto.SignupRequest;
import dev.jpitarch.ctrlgym.core.usecases.AuthUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

  private final AuthUseCase authUseCase;

  @PostMapping("/signup")
  public ResponseEntity<Void> signup(@RequestBody SignupRequest request) {
    authUseCase.signup(request);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PostMapping("/signin")
  public ResponseEntity<SigninResponse> signin(@RequestBody SigninRequest request) {
    return ResponseEntity.ok(authUseCase.signin(request));
  }
}
