package dev.jpitarch.ctrlgym.core.controllers.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jpitarch.ctrlgym.core.security.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

@Component
public class TenantFilter extends OncePerRequestFilter {

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/v1/auth");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain chain) throws IOException, ServletException {
    try {
      String tenantId = request.getHeader("X-Tenant-Id");
      String path = request.getRequestURI();

      if (path.startsWith("/v1/members/")) {
        if (tenantId == null) {
          writeProblemDetail(response);
          return;
        }
        TenantContextHolder.setTenantId(Integer.valueOf(tenantId));
      } else if (tenantId != null) {
        TenantContextHolder.setTenantId(Integer.valueOf(tenantId));
      }

      chain.doFilter(request, response);
    } finally {
      TenantContextHolder.clear();
    }
  }

  private void writeProblemDetail(HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Missing X-Tenant-Id header");
    problem.setTitle("Bad Request");
    problem.setType(URI.create("about:blank"));
    problem.setProperty("timestamp", Instant.now());

    new ObjectMapper().registerModule(new JavaTimeModule()).writeValue(response.getOutputStream(), problem);
  }
}
