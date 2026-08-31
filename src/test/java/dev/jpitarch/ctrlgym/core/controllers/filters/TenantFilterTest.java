package dev.jpitarch.ctrlgym.core.controllers.filters;

import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantFilterTest {

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Mock
  FilterChain filterChain;

  TenantFilter filter;

  @BeforeEach
  void setUp() {
    filter = new TenantFilter();
  }

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
  }

  @Test
  @DisplayName("shouldNotFilter returns true for non-matching paths")
  void shouldNotFilter_returnsTrueForNonMatchingPaths() {
    when(request.getRequestURI()).thenReturn("/v1/dashboard/metrics");

    assertTrue(filter.shouldNotFilter(request));
  }

  @Test
  @DisplayName("shouldNotFilter returns false for /v1/gyms paths")
  void shouldNotFilter_returnsFalseForGymsPaths() {
    when(request.getRequestURI()).thenReturn("/v1/gyms/1/branches");

    assertFalse(filter.shouldNotFilter(request));
  }

  @Test
  @DisplayName("shouldNotFilter returns false for /v1/members paths")
  void shouldNotFilter_returnsFalseForMembersPaths() {
    when(request.getRequestURI()).thenReturn("/v1/members/1/memberships");

    assertFalse(filter.shouldNotFilter(request));
  }

  @Test
  @DisplayName("Returns 400 when X-Tenant-Id header is missing")
  void doFilterInternal_returns400WhenTenantIdMissing() throws Exception {
    when(request.getHeader("X-Tenant-Id")).thenReturn(null);

    filter.doFilter(request, response, filterChain);

    verify(response).setStatus(400);
    verifyNoInteractions(filterChain);
  }

  @Test
  @DisplayName("Sets tenant id in context and continues chain")
  void doFilterInternal_setsTenantIdAndContinuesChain() throws Exception {
    when(request.getHeader("X-Tenant-Id")).thenReturn("42");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }

  @Test
  @DisplayName("Clears tenant context after filter execution")
  void doFilterInternal_clearsTenantContextAfterExecution() throws Exception {
    when(request.getHeader("X-Tenant-Id")).thenReturn("42");

    filter.doFilter(request, response, filterChain);

    assertNull(TenantContextHolder.getTenantId());
  }

  @Test
  @DisplayName("Clears tenant context even when chain throws exception")
  void doFilterInternal_clearsTenantContextOnException() throws Exception {
    when(request.getHeader("X-Tenant-Id")).thenReturn("42");
    doThrow(new RuntimeException("error")).when(filterChain).doFilter(request, response);

    assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, filterChain));
    assertNull(TenantContextHolder.getTenantId());
  }
}
