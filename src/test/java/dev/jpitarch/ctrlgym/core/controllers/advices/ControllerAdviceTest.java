package dev.jpitarch.ctrlgym.core.controllers.advices;

import dev.jpitarch.ctrlgym.core.domain.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ControllerAdviceTest {

  private MockMvc mockMvc;

  @RestController
  static class TestController {
    @GetMapping("/member-not-found")
    public void throwMemberNotFoundException() {
      throw new MemberNotFoundException(java.util.UUID.randomUUID());
    }

    @GetMapping("/member-without-access")
    public void throwMemberWithoutAccessException() {
      throw new MemberWithoutAccessException(java.util.UUID.randomUUID());
    }

    @GetMapping("/core-business")
    public void throwCoreBusinessException() {
      throw new CoreBusinessException(Object.class, "business error");
    }


    @GetMapping("/auth")
    public void throwAuthException() {
      throw new AuthException(AuthException.Signup.ALREADY_EXISTS, 1, "test@example.com");
    }

    @GetMapping("/auth-in-migration")
    public void throwAuthExceptionInMigration() {
      throw new AuthException(AuthException.Signup.IS_IN_MIGRATION, 1, "test@example.com");
    }

    @GetMapping("/auth-another-gym")
    public void throwAuthExceptionAnotherGym() {
      throw new AuthException(AuthException.Signup.ANOTHER_GYM, 1, "test@example.com");
    }
  }

  @BeforeEach
  void setUp() {
    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    ControllerAdvice advice = new ControllerAdvice(eventPublisher);
    mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
      .setControllerAdvice(advice)
      .build();
  }

  @Test
  @DisplayName("MemberNotFoundException returns 404")
  void memberNotFoundException_returns404() throws Exception {
    mockMvc.perform(get("/member-not-found"))
      .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("MemberWithoutAccessException returns 409")
  void memberWithoutAccessException_returns409() throws Exception {
    mockMvc.perform(get("/member-without-access"))
      .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("CoreBusinessException returns 422")
  void coreBusinessException_returns422() throws Exception {
    mockMvc.perform(get("/core-business"))
      .andExpect(status().is(HttpStatus.UNPROCESSABLE_CONTENT.value()));
  }

  @Test
  @DisplayName("AuthException returns 409 with detail equal to signup name")
  void authException_returns409WithDetail() throws Exception {
    mockMvc.perform(get("/auth"))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.detail").value("ALREADY_EXISTS"));
  }

  @Test
  @DisplayName("AuthException with IS_IN_MIGRATION returns 409 with detail equal to signup name")
  void authExceptionInMigration_returns409WithDetail() throws Exception {
    mockMvc.perform(get("/auth-in-migration"))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.detail").value("IS_IN_MIGRATION"));
  }

  @Test
  @DisplayName("AuthException with ANOTHER_GYM returns 409 with detail equal to signup name")
  void authExceptionAnotherGym_returns409WithDetail() throws Exception {
    mockMvc.perform(get("/auth-another-gym"))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.detail").value("ANOTHER_GYM"));
  }
}
