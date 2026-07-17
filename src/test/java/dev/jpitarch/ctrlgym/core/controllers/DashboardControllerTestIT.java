package dev.jpitarch.ctrlgym.core.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DashboardControllerTestIT extends BaseIntegrationTest {

  @Test
  @Order(1)
  @DisplayName("Returns active memberships count")
  void getMemberships_withFlowActive_returns200() throws Exception {
    mockMvc.perform(get("/v1/dashboard/gyms/{gymId}/branches/{branchId}/memberships", 1, 1)
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
    mockMvc.perform(get("/v1/dashboard/gyms/{gymId}/branches/{branchId}/memberships", 1, 1)
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
    mockMvc.perform(get("/v1/dashboard/gyms/{gymId}/branches/{branchId}/memberships", 1, 1)
        .param("from", "2026-01-01")
        .param("to", "2026-06-01")
        .param("flow", "CANCELLED")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

}
