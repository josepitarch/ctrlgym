package dev.jpitarch.ctrlgym.authentication.controllers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.jpitarch.ctrlgym.authentication.dtos.ForgotPasswordRequest;
import dev.jpitarch.ctrlgym.authentication.dtos.RefreshRequest;
import dev.jpitarch.ctrlgym.authentication.dtos.LoginRequest;
import dev.jpitarch.ctrlgym.authentication.dtos.ResetPasswordRequest;
import dev.jpitarch.ctrlgym.authentication.dtos.SignupRequest;
import dev.jpitarch.ctrlgym.authentication.repositories.UserRepository;
import dev.jpitarch.ctrlgym.core.controllers.BaseIntegrationTest;
import dev.jpitarch.ctrlgym.notifications.EmailTemplateComponent;
import dev.jpitarch.ctrlgym.notifications.services.EmailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTestIT extends BaseIntegrationTest {

  JsonMapper objectMapper = new JsonMapper();

  static String accessToken;
  static String refreshToken;

  @Value("${jwt.secret}")
  String jwtSecret;

  @Autowired
  UserRepository userRepository;

  @MockitoBean
  EmailTemplateComponent emailTemplateComponent;

  @MockitoBean
  EmailService emailService;

  @BeforeEach
  void setUp() {
    when(emailTemplateComponent.build(anyString(), any())).thenReturn("<html></html>");
  }

  @Test
  @Order(1)
  @DisplayName("Signup returns tokens")
  void signup_returns200_withTokens() throws Exception {
    var request = new SignupRequest(
      "newuser@test.com",
      "Password1!",
      "New",
      "User",
      null
    );

    MvcResult result = mockMvc.perform(post("/v1/auth/signup")
        .header("X-Tenant-Id", 1)
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
    var request = new LoginRequest("newuser@test.com", "Password1!");

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
    var request = new LoginRequest("newuser@test.com", "WrongPassword!");

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

  @Test
  @Order(6)
  @DisplayName("Forgot password returns 200 and sends email")
  void forgotPassword_returns200_sendsEmail() throws Exception {
    var request = new ForgotPasswordRequest("newuser@test.com");

    mockMvc.perform(post("/v1/auth/password/forgot")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk());

    verify(emailTemplateComponent).build(eq("password-reset.html"), any());
    verify(emailService).send(eq("newuser@test.com"), anyString(), anyString());
  }

  @Test
  @Order(7)
  @DisplayName("Reset password with valid token returns 200")
  void resetPassword_returns200() throws Exception {
    var user = userRepository.findByEmail("newuser@test.com").orElseThrow();
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    String token = Jwts.builder()
      .subject(user.getId().toString())
      .claim("type", "password_reset")
      .claim("email", "newuser@test.com")
      .issuedAt(Date.from(Instant.now()))
      .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
      .signWith(key)
      .compact();

    var request = new ResetPasswordRequest(token, "NewPassword1!");

    mockMvc.perform(post("/v1/auth/password/reset")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk());
  }
}
