package dev.jpitarch.ctrlgym.core.dto;

public record SignupRequest(String email, String password, Integer gymId, String name, String firstSurname, String secondSurname) {
}
