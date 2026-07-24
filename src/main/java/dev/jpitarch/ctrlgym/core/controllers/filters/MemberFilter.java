package dev.jpitarch.ctrlgym.core.controllers.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.server.PathContainer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;

@Component
public class MemberFilter extends OncePerRequestFilter {

  private static final PathPattern MEMBER_PATTERN = new PathPatternParser().parse("/v1/members/{memberId}/*");

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/v1/members");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain chain) throws IOException, ServletException {

    PathContainer path = PathContainer.parsePath(request.getRequestURI());

    if (MEMBER_PATTERN.matches(path)) {
      var matchInfo = MEMBER_PATTERN.matchAndExtract(path);
      if (matchInfo != null) {
        String pathMemberId = matchInfo.getUriVariables().get("memberId");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
          String sub = jwt.getSubject();
          if (!pathMemberId.equals(sub)) {
            response.setStatus(403);
            return;
          }
        }
      }
    }

    chain.doFilter(request, response);
  }
}
