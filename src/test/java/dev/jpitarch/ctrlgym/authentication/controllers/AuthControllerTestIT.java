package dev.jpitarch.ctrlgym.authentication.controllers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.jpitarch.ctrlgym.authentication.dtos.RefreshRequest;
import dev.jpitarch.ctrlgym.authentication.dtos.SigninRequest;
import dev.jpitarch.ctrlgym.authentication.dtos.SignupRequest;
import dev.jpitarch.ctrlgym.core.controllers.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTestIT extends BaseIntegrationTest {

  JsonMapper objectMapper = new JsonMapper();

  static String accessToken;
  static String refreshToken;

  @Test
  @Order(1)
  @DisplayName("Signup returns tokens")
  void signup_returns200_withTokens() throws Exception {
    var request = new SignupRequest(
      "newuser@test.com",
      "Password1!",
      1,
      "New",
      "User",
      null
    );

    MvcResult result = mockMvc.perform(post("/v1/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.access_token").isNotEmpty())
      .andExpect(jsonPath("$.refresh_token").isNotEmpty())
      .andExpect(jsonPath("$.expires_in").isNumber())
      .andExpect(jsonPath("$.token_type").value("Bearer"))
      .andReturn();

    var response = objectMapper.readTree(result.getResponse().getContentAsString());
    accessToken = response.get("access_token").asText();
    refreshToken = response.get("refresh_token").asText();
  }

  @Test
  @Order(2)
  @DisplayName("Login with valid credentials returns tokens")
  void login_returns200_withTokens() throws Exception {
    var request = new SigninRequest("newuser@test.com", "Password1!");

    MvcResult result = mockMvc.perform(post("/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.access_token").isNotEmpty())
      .andExpect(jsonPath("$.refresh_token").isNotEmpty())
      .andExpect(jsonPath("$.expires_in").isNumber())
      .andExpect(jsonPath("$.token_type").value("Bearer"))
      .andReturn();

    var response = objectMapper.readTree(result.getResponse().getContentAsString());
    accessToken = response.get("access_token").asText();
    refreshToken = response.get("refresh_token").asText();
  }

  @Test
  @Order(3)
  @DisplayName("Login with invalid credentials returns 400")
  void login_invalidCredentials_returns400() throws Exception {
    var request = new SigninRequest("newuser@test.com", "WrongPassword!");

    mockMvc.perform(post("/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @Order(4)
  @DisplayName("Refresh with valid token returns new tokens")
  void refresh_returns200_withNewTokens() throws Exception {
    var request = new RefreshRequest(refreshToken);

    mockMvc.perform(post("/v1/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.access_token").isNotEmpty())
      .andExpect(jsonPath("$.refresh_token").isNotEmpty())
      .andExpect(jsonPath("$.token_type").value("Bearer"));
  }

  @Test
  @Order(5)
  @DisplayName("Logout with valid refresh token returns 200")
  void logout_returns200() throws Exception {
    var request = new RefreshRequest(refreshToken);

    mockMvc.perform(post("/v1/auth/logout")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk());
  }
}
