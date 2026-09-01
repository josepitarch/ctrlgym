package dev.jpitarch.ctrlgym.authentication.dtos;

public record ResetPasswordRequest(String token, String password) {
}
