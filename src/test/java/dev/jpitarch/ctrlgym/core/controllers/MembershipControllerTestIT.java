package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.security.CustomJwtAuthenticationToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MembershipControllerTestIT extends BaseIntegrationTest {

  private RequestPostProcessor jwtAuth() {
    Jwt jwt = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("user_metadata", Map.of("gym_id", 1))
      .claim("user_roles", List.of("MANAGER"))
      .subject(UUID.randomUUID().toString())
      .issuedAt(Instant.now())
      .build();

    var authorities = List.of(new SimpleGrantedAuthority("ROLE_MANAGER"));
    var authenticationToken = new CustomJwtAuthenticationToken(jwt, authorities, 1);

    return authentication(authenticationToken);
  }

  @Test
  @DisplayName("Returns all cancellation reasons")
  void getAllCancellationReasons_returnsAllReasons() throws Exception {
    mockMvc.perform(get("/v1/memberships/cancellation-reasons")
        .with(jwtAuth())
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.length()").value(16))
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].name").value("Precio demasiado alto"))
      .andExpect(jsonPath("$[0].description").value("La cuota o los servicios son demasiado caros"))
      .andExpect(jsonPath("$[1].id").value(2))
      .andExpect(jsonPath("$[1].name").value("No utiliza el gimnasio"));
  }
}
