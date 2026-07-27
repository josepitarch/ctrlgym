package dev.jpitarch.ctrlgym.core.dto;

public record SigninResponse(String accessToken, String refreshToken, Integer expiresIn, String tokenType) {
}
