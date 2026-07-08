package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.models.MembershipMO;
import dev.jpitarch.ctrlgym.core.models.RoutineMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberControllerTestIT extends BaseIntegrationTest {

  @MockitoBean
  SubscriptionService subscriptionService;

  @Autowired
  MembershipJpaRepository membershipJpaRepository;

  @Autowired
  RoutineJpaRepository routineJpaRepository;

  ObjectMapper objectMapper = new ObjectMapper();

  private final UUID memberId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
  private final Integer gymId = 1;

  @Test
  @Order(1)
  @DisplayName("Returns an existing member")
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
  @DisplayName("Returns 404 when member not found")
  void getMember_returns404_whenMemberNotFound() throws Exception {
    var nonExistentId = UUID.randomUUID();

    mockMvc.perform(get("/v1/members/{memberId}", nonExistentId)
                    .param("gymId", gymId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  @Order(3)
  @DisplayName("Returns 409 when generating QR without active membership")
  void generateQr_returns409_whenMemberHasNoActiveMembership() throws Exception {
    mockMvc.perform(post("/v1/members/{memberId}/generate-qr", memberId)
                    .param("gymId", gymId.toString()))
            .andExpect(status().isConflict());
  }

  @Test
  @Order(4)
  @DisplayName("Initializes membership successfully")
  void initializeMembership_returns204_whenSuccessful() throws Exception {
    when(subscriptionService.create(any(), any())).thenReturn("sub_test123");

    mockMvc.perform(post("/v1/members/{memberId}/memberships/{membershipId}", memberId, "plan_premium")
                    .param("gymId", gymId.toString()))
            .andExpect(status().isNoContent());
  }

@Test
  @Order(5)
  @DisplayName("Returns all memberships for a member")
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
  @DisplayName("Cancels membership successfully")
  void cancelMembership_returns204() throws Exception {
    mockMvc.perform(patch("/v1/members/{memberId}/memberships/{membershipId}", memberId, "plan_basic")
                    .param("gymId", gymId.toString())
                    .param("cancellationReasonId", "1"))
            .andExpect(status().isNoContent());

    MembershipMO membership = membershipJpaRepository.findById(1L).orElseThrow();
    assertThat(membership.getEndDate()).isNotNull();
    assertThat(membership.getCancellationReasonId()).isEqualTo(1);
  }

  @Test
  @Order(7)
  @DisplayName("Creates a routine successfully")
  void createRoutine_returns201() throws Exception {
    var routineJson = objectMapper.readTree(new ClassPathResource("fixtures/routine_push_pull_legs.json").getInputStream()).toString();

    mockMvc.perform(post("/v1/members/{memberId}/routines", memberId)
                    .param("gymId", gymId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(routineJson))
            .andExpect(status().isCreated());
  }

  @Test
  @Order(8)
  @DisplayName("Returns member routines")
  void getRoutines_returnsRoutines() throws Exception {
    mockMvc.perform(get("/v1/members/{memberId}/routines", memberId)
                    .param("gymId", gymId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Push Pull Legs"))
            .andExpect(jsonPath("$[0].days.length()").value(2))
            .andExpect(jsonPath("$[0].days[0].name").value("Push"))
            .andExpect(jsonPath("$[0].days[0].exercises.length()").value(2))
            .andExpect(jsonPath("$[0].days[0].exercises[0].name").value("Press de banca"))
            .andExpect(jsonPath("$[0].days[0].exercises[0].sets.length()").value(3));
  }

  @Test
  @Order(9)
  @DisplayName("Deletes a routine successfully")
  void deleteRoutine_returns204() throws Exception {
    RoutineMO routine = routineJpaRepository.findByMemberIdAndGymId(memberId, gymId).getFirst();

    mockMvc.perform(delete("/v1/members/{memberId}/routines/{routineId}", memberId, routine.getId())
                    .param("gymId", gymId.toString()))
            .andExpect(status().isNoContent());

    assertThat(routineJpaRepository.findByMemberIdAndGymId(memberId, gymId)).isEmpty();
  }
}
