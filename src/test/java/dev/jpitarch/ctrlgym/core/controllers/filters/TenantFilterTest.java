package dev.jpitarch.ctrlgym.core.controllers.filters;

import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;

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
  @DisplayName("shouldNotFilter returns true for /v1/auth paths")
  void shouldNotFilter_returnsTrueForAuthPaths() {
    when(request.getRequestURI()).thenReturn("/v1/auth/login");

    assertTrue(filter.shouldNotFilter(request));
  }

  @Test
  @DisplayName("shouldNotFilter returns false for /v1/members/ paths")
  void shouldNotFilter_returnsFalseForMembersPaths() {
    when(request.getRequestURI()).thenReturn("/v1/members/1/memberships");

    assertFalse(filter.shouldNotFilter(request));
  }

  @Test
  @DisplayName("shouldNotFilter returns false for other paths")
  void shouldNotFilter_returnsFalseForOtherPaths() {
    when(request.getRequestURI()).thenReturn("/v1/dashboard/metrics");

    assertFalse(filter.shouldNotFilter(request));
  }

  @Test
  @DisplayName("Returns 400 when X-Tenant-Id header is missing for /v1/members/")
  void doFilterInternal_returns400WhenTenantIdMissingForMembers() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/members/1/memberships");
    when(request.getHeader("X-Tenant-Id")).thenReturn(null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ServletOutputStream servletOutputStream = new ServletOutputStream() {
      @Override
      public boolean isReady() { return true; }
      @Override
      public void setWriteListener(WriteListener writeListener) {}
      @Override
      public void write(int b) { baos.write(b); }
    };
    when(response.getOutputStream()).thenReturn(servletOutputStream);

    filter.doFilter(request, response, filterChain);

    verify(response).setStatus(400);
    verifyNoInteractions(filterChain);
  }

  @Test
  @DisplayName("Sets tenant id in context and continues chain for /v1/members/")
  void doFilterInternal_setsTenantIdAndContinuesChainForMembers() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/members/1/memberships");
    when(request.getHeader("X-Tenant-Id")).thenReturn("42");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }

  @Test
  @DisplayName("Continues chain without setting tenant for other paths without header")
  void doFilterInternal_continuesChainWithoutTenantForOtherPaths() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/gyms/1/exercises");
    when(request.getHeader("X-Tenant-Id")).thenReturn(null);

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
    assertNull(TenantContextHolder.getTenantId());
  }

  @Test
  @DisplayName("Sets tenant id in context for other paths when header is present")
  void doFilterInternal_setsTenantIdForOtherPathsWithHeader() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/gyms/1/exercises");
    when(request.getHeader("X-Tenant-Id")).thenReturn("42");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }

  @Test
  @DisplayName("Clears tenant context after filter execution for /v1/members/")
  void doFilterInternal_clearsTenantContextAfterExecutionForMembers() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/members/1/memberships");
    when(request.getHeader("X-Tenant-Id")).thenReturn("42");

    filter.doFilter(request, response, filterChain);

    assertNull(TenantContextHolder.getTenantId());
  }

  @Test
  @DisplayName("Clears tenant context after filter execution for other paths")
  void doFilterInternal_clearsTenantContextAfterExecutionForOtherPaths() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/gyms/1/exercises");
    when(request.getHeader("X-Tenant-Id")).thenReturn("42");

    filter.doFilter(request, response, filterChain);

    assertNull(TenantContextHolder.getTenantId());
  }

  @Test
  @DisplayName("Clears tenant context even when chain throws exception")
  void doFilterInternal_clearsTenantContextOnException() throws Exception {
    when(request.getRequestURI()).thenReturn("/v1/members/1/memberships");
    when(request.getHeader("X-Tenant-Id")).thenReturn("42");
    doThrow(new RuntimeException("error")).when(filterChain).doFilter(request, response);

    assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, filterChain));
    assertNull(TenantContextHolder.getTenantId());
  }
}
