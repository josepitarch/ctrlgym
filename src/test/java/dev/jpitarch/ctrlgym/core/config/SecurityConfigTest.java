package dev.jpitarch.ctrlgym.core.config;

import dev.jpitarch.ctrlgym.core.controllers.DashboardController;
import dev.jpitarch.ctrlgym.core.controllers.MemberController;
import dev.jpitarch.ctrlgym.core.controllers.filters.ControllerApiKeyFilter;
import dev.jpitarch.ctrlgym.core.security.CustomJwtAuthenticationToken;
import dev.jpitarch.ctrlgym.core.usecases.DashboardUseCase;
import dev.jpitarch.ctrlgym.core.usecases.MemberUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WebMvcTest({DashboardController.class, MemberController.class})
class SecurityConfigTest {

  @MockitoBean
  DashboardUseCase dashboardUseCase;

  @MockitoBean
  MemberUseCase memberUseCase;

  @MockitoBean
  ControllerApiKeyFilter controllerApiKeyFilter;

  @Autowired
  private MockMvc mockMvc;

  private CustomJwtAuthenticationToken createJwtToken(Integer gymId, String subject, String role) {
    Jwt jwt = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("user_metadata", Map.of("gym_id", gymId))
      .claim("user_roles", List.of(role))
      .subject(subject)
      .issuedAt(Instant.now())
      .build();

    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    return new CustomJwtAuthenticationToken(jwt, authorities, gymId);
  }

  @Test
  @DisplayName("Dashboard without authentication returns 401")
  void dashboard_withoutAuth_returns401() throws Exception {
    mockMvc.perform(get("/v1/dashboard/gyms/1/metrics")
        .param("from", "2026-01")
        .param("to", "2026-06"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Dashboard with MEMBER role returns 403")
  void dashboard_withMemberRole_returns403() throws Exception {
    mockMvc.perform(get("/v1/dashboard/gyms/1/metrics")
        .param("from", "2026-01")
        .param("to", "2026-06")
        .with(authentication(createJwtToken(1, UUID.randomUUID().toString(), "MEMBER"))))
      .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Dashboard with MANAGER role passes security")
  void dashboard_withManagerRole_passesSecurity() throws Exception {
    mockMvc.perform(get("/v1/dashboard/gyms/1/metrics")
        .param("from", "2026-01")
        .param("to", "2026-06")
        .with(authentication(createJwtToken(1, UUID.randomUUID().toString(), "MANAGER"))))
      .andExpect(status().is2xxSuccessful());
  }

  @Test
  @DisplayName("Dashboard with mismatched gymId returns 403")
  void dashboard_withMismatchedGymId_returns403() throws Exception {
    mockMvc.perform(get("/v1/dashboard/gyms/1/metrics")
        .param("from", "2026-01")
        .param("to", "2026-06")
        .with(authentication(createJwtToken(999, UUID.randomUUID().toString(), "MANAGER"))))
      .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Members without authentication returns 401")
  void members_withoutAuth_returns401() throws Exception {
    mockMvc.perform(get("/v1/members/00000000-0000-0000-0000-000000000001")
        .param("gymId", "1"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Members with MANAGER role returns 403")
  void members_withManagerRole_returns403() throws Exception {
    mockMvc.perform(get("/v1/members/00000000-0000-0000-0000-000000000001")
        .param("gymId", "1")
        .with(authentication(createJwtToken(1, UUID.randomUUID().toString(), "MANAGER"))))
      .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Members with MEMBER role passes security")
  void members_withMemberRole_passesSecurity() throws Exception {
    String memberId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    mockMvc.perform(get("/v1/members/" + memberId)
        .param("gymId", "1")
        .with(authentication(createJwtToken(1, memberId, "MEMBER"))))
      .andExpect(status().is2xxSuccessful());
  }

  @Test
  @DisplayName("Members with mismatched memberId returns 403")
  void members_withMismatchedMemberId_returns403() throws Exception {
    mockMvc.perform(get("/v1/members/a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        .param("gymId", "1")
        .with(authentication(createJwtToken(1, "00000000-0000-0000-0000-000000000099", "MEMBER"))))
      .andExpect(status().isForbidden());
  }
}
