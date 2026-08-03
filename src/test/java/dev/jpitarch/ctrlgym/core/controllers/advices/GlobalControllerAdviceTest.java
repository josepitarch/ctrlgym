package dev.jpitarch.ctrlgym.core.controllers.advices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalControllerAdviceTest {

  private MockMvc mockMvc;

  @RestController
  static class TestController {
    @GetMapping("/illegal-argument")
    public void throwIllegalArgumentException() {
      throw new IllegalArgumentException("bad argument");
    }

    @GetMapping("/authorization-denied")
    public void throwAuthorizationDeniedException() {
      throw new AuthorizationDeniedException("access denied");
    }

    @GetMapping("/generic")
    public void throwGenericException() {
      throw new RuntimeException("unexpected error");
    }
  }

  @BeforeEach
  void setUp() {
    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    GlobalControllerAdvice advice = new GlobalControllerAdvice(eventPublisher);
    mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
      .setControllerAdvice(advice)
      .build();
  }

  @Test
  @DisplayName("IllegalArgumentException returns 400")
  void illegalArgumentException_returns400() throws Exception {
    mockMvc.perform(get("/illegal-argument"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("AuthorizationDeniedException returns 403")
  void authorizationDeniedException_returns403() throws Exception {
    mockMvc.perform(get("/authorization-denied"))
      .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Generic Exception returns 500")
  void genericException_returns500() throws Exception {
    mockMvc.perform(get("/generic"))
      .andExpect(status().isInternalServerError());
  }
}
