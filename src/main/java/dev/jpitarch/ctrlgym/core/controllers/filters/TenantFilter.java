package dev.jpitarch.ctrlgym.core.controllers.filters;

import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();
    return !path.startsWith("/v1/gyms") && !path.startsWith("/v1/members");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain chain) throws IOException, ServletException {
    try {
      String tenantId = request.getHeader("X-Tenant-Id");

      if (tenantId == null) {
        response.setStatus(400);
        return;
      }

      TenantContextHolder.setTenantId(Integer.valueOf(tenantId));
      chain.doFilter(request, response);
    } finally {
      TenantContextHolder.clear();
    }
  }
}
