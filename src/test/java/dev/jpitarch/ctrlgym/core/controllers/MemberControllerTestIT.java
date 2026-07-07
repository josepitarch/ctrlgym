package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberControllerTestIT extends BaseIntegrationTest {

  @MockitoBean
  SubscriptionService subscriptionService;

  private final UUID memberId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
  private final Integer gymId = 1;

  @Test
  void getMember_returnsMember() throws Exception {
    mockMvc.perform(get("/v1/members/{memberId}", memberId)
                    .param("gymId", gymId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id.member_id").value(memberId.toString()))
            .andExpect(jsonPath("$.id.gym_id").value(gymId))
            .andExpect(jsonPath("$.name").value("John"))
            .andExpect(jsonPath("$.first_surname").value("Doe"))
            .andExpect(jsonPath("$.second_surname").value("Smith"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.gender").value("MALE"));
  }

  @Test
  void getMember_returns404_whenMemberNotFound() throws Exception {
    var nonExistentId = UUID.randomUUID();

    mockMvc.perform(get("/v1/members/{memberId}", nonExistentId)
                    .param("gymId", gymId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  void generateQr_returns409_whenMemberHasNoActiveMembership() throws Exception {
    mockMvc.perform(post("/v1/members/{memberId}/generate-qr", memberId)
                    .param("gymId", gymId.toString()))
            .andExpect(status().isConflict());
  }

  @Test
  void initializeMembership_returns204_whenSuccessful() throws Exception {
    when(subscriptionService.createSubscription(any(), any())).thenReturn("sub_test123");

    mockMvc.perform(post("/v1/members/{memberId}/memberships/{membershipId}", memberId, "plan_premium")
                    .param("gymId", gymId.toString()))
            .andExpect(status().isNoContent());
  }
}
