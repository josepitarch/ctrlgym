package dev.jpitarch.ctrlgym.core.controllers.filters;

import dev.jpitarch.ctrlgym.core.repositories.GymsRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.server.PathContainer;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ControllerApiKeyFilter extends OncePerRequestFilter {

  private final GymsRepository gymsRepository;

  private static final PathPattern GYM_BRANCH_PATTERN = new PathPatternParser().parse("/v1/controllers/{gymBranchId}/*");

  public ControllerApiKeyFilter(GymsRepository gymsRepository) {
    this.gymsRepository = gymsRepository;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    return request.getRequestURI().endsWith("/health");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain chain) throws IOException, ServletException {

    String key = request.getHeader("X-API-Key");

    if (key == null) {
      response.setStatus(400);
      return;
    }

    PathContainer path = PathContainer.parsePath(request.getRequestURI());

    if (GYM_BRANCH_PATTERN.matches(path)) {
      PathPattern.PathMatchInfo matchInfo = GYM_BRANCH_PATTERN.matchAndExtract(path);
      if (matchInfo != null) {
        Integer gymBranchId = Integer.valueOf(matchInfo.getUriVariables().get("gymBranchId"));

        if (!this.hash(key).equals(gymsRepository.getControllerApiKey(gymBranchId))) {
          response.setStatus(401);
          return;
        }

      }

    }

    chain.doFilter(request, response);
  }

  private String hash(String apiKey) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(apiKey.getBytes());
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}