package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.security.CustomJwtAuthenticationToken;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
class DashboardControllerTestIT extends BaseIntegrationTest {

  Integer gymId = 1;

  private RequestPostProcessor jwtAuth() {
    Jwt jwt = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("gym_id", gymId)
      .claim("role", "MANAGER")
      .subject(UUID.randomUUID().toString())
      .issuedAt(Instant.now())
      .build();

    var authorities = List.of(new SimpleGrantedAuthority("ROLE_MANAGER"));
    var authenticationToken = new CustomJwtAuthenticationToken(jwt, authorities, gymId);

    return authentication(authenticationToken);
  }

  @Test
  @Order(1)
  @DisplayName("Returns active memberships count")
  void getMemberships_withFlowActive_returns200() throws Exception {
    mockMvc.perform(get("/v1/dashboard/gyms/{gymId}/branches/{branchId}/memberships", gymId, 1)
        .with(jwtAuth())
        .param("from", "2026-01-01")
        .param("to", "2026-06-01")
        .param("flow", "ACTIVE")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @Order(2)
  @DisplayName("Returns new memberships count")
  void getMemberships_withFlowNew_returns200() throws Exception {
    mockMvc.perform(get("/v1/dashboard/gyms/{gymId}/branches/{branchId}/memberships", gymId, 1)
        .with(jwtAuth())
        .param("from", "2026-01-01")
        .param("to", "2026-06-01")
        .param("flow", "NEW")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @Order(3)
  @DisplayName("Returns cancelled memberships count")
  void getMemberships_withFlowCancelled_returns200() throws Exception {
    mockMvc.perform(get("/v1/dashboard/gyms/{gymId}/branches/{branchId}/memberships", gymId, 1)
        .with(jwtAuth())
        .param("from", "2026-01-01")
        .param("to", "2026-06-01")
        .param("flow", "CANCELLED")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

}
