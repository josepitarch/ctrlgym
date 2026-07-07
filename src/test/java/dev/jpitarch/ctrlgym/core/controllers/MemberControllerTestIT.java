package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.models.MembershipMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipJpaRepository;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberControllerTestIT extends BaseIntegrationTest {

  @MockitoBean
  SubscriptionService subscriptionService;

  @Autowired
  MembershipJpaRepository membershipJpaRepository;

  private final UUID memberId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
  private final Integer gymId = 1;

  @Test
  @Order(1)
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
  @Order(2)
  void getMember_returns404_whenMemberNotFound() throws Exception {
    var nonExistentId = UUID.randomUUID();

    mockMvc.perform(get("/v1/members/{memberId}", nonExistentId)
                    .param("gymId", gymId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  @Order(3)
  void generateQr_returns409_whenMemberHasNoActiveMembership() throws Exception {
    mockMvc.perform(post("/v1/members/{memberId}/generate-qr", memberId)
                    .param("gymId", gymId.toString()))
            .andExpect(status().isConflict());
  }

  @Test
  @Order(4)
  void initializeMembership_returns204_whenSuccessful() throws Exception {
    when(subscriptionService.create(any(), any())).thenReturn("sub_test123");

    mockMvc.perform(post("/v1/members/{memberId}/memberships/{membershipId}", memberId, "plan_premium")
                    .param("gymId", gymId.toString()))
            .andExpect(status().isNoContent());

    MembershipMO membership = membershipJpaRepository
            .findByMemberIdAndGymIdAndMembershipPlanIdAndEndDateIsNull(memberId, gymId, "plan_premium")
            .orElseThrow();
    assertThat(membership.getStripeSubscriptionId()).isEqualTo("sub_test123");
  }

@Test
  @Order(5)
  void getMemberships_returnsAllMemberships() throws Exception {
    mockMvc.perform(get("/v1/members/{memberId}/memberships", memberId)
                    .param("gymId", gymId.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].interval").value("MONTHLY"))
            .andExpect(jsonPath("$[0].next_billing_date").isNotEmpty());
  }

  @Test
  @Order(6)
  void cancelMembership_returns204() throws Exception {
    mockMvc.perform(patch("/v1/members/{memberId}/memberships/{membershipId}", memberId, "plan_basic")
                    .param("gymId", gymId.toString())
                    .param("cancellationReasonId", "1"))
            .andExpect(status().isNoContent());

    MembershipMO membership = membershipJpaRepository.findById(1L).orElseThrow();
    assertThat(membership.getEndDate()).isNotNull();
    assertThat(membership.getCancellationReasonId()).isEqualTo(1);
  }
}
