package dev.jpitarch.ctrlgym.core.controllers.filters;

import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.util.Base64;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllerApiKeyFilterTest {

  @Mock
  GymsRepository gymsRepository;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  FilterChain filterChain;

  ControllerApiKeyFilter filter;

  @BeforeEach
  void setUp() {
    filter = new ControllerApiKeyFilter(gymsRepository);
  }

  @Test
  @DisplayName("shouldNotFilter returns true for health endpoint")
  void shouldNotFilter_returnsTrueForHealthEndpoint() {
    when(request.getRequestURI()).thenReturn("/v1/controllers/1/health");

    boolean result = filter.shouldNotFilter(request);

    assert result;
  }

  @Test
  @DisplayName("shouldNotFilter returns false for non-health endpoint")
  void shouldNotFilter_returnsFalseForNonHealthEndpoint() {
    when(request.getRequestURI()).thenReturn("/v1/controllers/1/members");

    boolean result = filter.shouldNotFilter(request);

    assert !result;
  }

  @Test
  @DisplayName("Returns 400 when API key header is missing")
  void doFilterInternal_returns400WhenApiKeyMissing() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/controllers/1/members");
    when(request.getHeader("X-API-Key")).thenReturn(null);

    filter.doFilter(request, response, filterChain);

    verify(response).setStatus(400);
    verifyNoInteractions(filterChain);
  }

  @Test
  @DisplayName("Returns 401 when API key does not match for gym branch path")
  void doFilterInternal_returns401WhenApiKeyDoesNotMatch() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/controllers/1/members");
    when(request.getHeader("X-API-Key")).thenReturn("invalid-key");
    when(gymsRepository.getControllerApiKey(1)).thenReturn(hash());

    filter.doFilter(request, response, filterChain);

    verify(response).setStatus(401);
    verifyNoInteractions(filterChain);
  }

  @Test
  @DisplayName("Continues filter chain when API key matches for gym branch path")
  void doFilterInternal_continuesWhenApiKeyMatches() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/controllers/1/members");
    when(request.getHeader("X-API-Key")).thenReturn("valid-key");
    when(gymsRepository.getControllerApiKey(1)).thenReturn(hash());

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }

  @Test
  @DisplayName("Continues filter chain for non-gym-branch paths with API key")
  void doFilterInternal_continuesForNonGymBranchPathsWithApiKey() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/gyms/1/branches");
    when(request.getHeader("X-API-Key")).thenReturn("any-key");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(gymsRepository);
  }

  private String hash() {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest("valid-key".getBytes());
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
