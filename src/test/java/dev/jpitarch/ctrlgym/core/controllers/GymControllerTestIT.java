package dev.jpitarch.ctrlgym.core.controllers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.domain.enums.MuscleGroup;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExerciseJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipPlanJpaRepository;
import dev.jpitarch.ctrlgym.core.security.CustomJwtAuthenticationToken;
import dev.jpitarch.ctrlgym.payments.services.ProductService;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GymControllerTestIT extends BaseIntegrationTest {

  @MockitoBean
  ProductService productService;

  @MockitoBean
  SubscriptionService subscriptionService;

  @Autowired
  ExerciseJpaRepository exerciseJpaRepository;

  @Autowired
  MembershipPlanJpaRepository membershipPlanJpaRepository;

  JsonMapper objectMapper = new JsonMapper();

  Integer gymId = 1;

  private RequestPostProcessor jwtAuth() {
    Jwt jwt = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("user_metadata", Map.of("gym_id", gymId))
      .claim("user_roles", List.of("MANAGER"))
      .subject(UUID.randomUUID().toString())
      .issuedAt(Instant.now())
      .build();

    var authorities = List.of(new SimpleGrantedAuthority("ROLE_MANAGER"));
    var authentication = new CustomJwtAuthenticationToken(jwt, authorities, gymId);

    return authentication(authentication);
  }

  @Test
  @Order(1)
  @DisplayName("Creates an exercise successfully")
  void createExercise_returns201() throws Exception {
    var exercise = Exercise.builder()
      .name("Curl de biceps")
      .description("Ejercicio para biceps")
      .muscleGroup(MuscleGroup.BICEPS)
      .image("https://example.com/curl.jpg")
      .build();

    mockMvc.perform(post("/v1/gyms/{gymId}/exercises", gymId)
        .with(jwtAuth())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(exercise)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.name").value("Curl de biceps"))
      .andExpect(jsonPath("$.muscle_group").value("BICEPS"));
  }

  @Test
  @Order(2)
  @DisplayName("Returns all gym exercises")
  void getExercises_returnsAllExercises() throws Exception {
    mockMvc.perform(get("/v1/gyms/{gymId}/exercises", gymId)
        .with(jwtAuth())
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(21))
      .andExpect(jsonPath("$[0].name").value("Press de banca"))
      .andExpect(jsonPath("$[0].muscle_group").value("CHEST"));
  }

  @Test
  @Order(3)
  @DisplayName("Deletes an exercise successfully")
  void deleteExercise_returns204() throws Exception {
    mockMvc.perform(delete("/v1/gyms/{gymId}/exercises/{exerciseId}", gymId, 21)
        .with(jwtAuth()))
      .andExpect(status().isNoContent());

    assertThat(exerciseJpaRepository.findById(21)).isEmpty();
  }

  @Test
  @Order(4)
  @DisplayName("Creates a membership plan successfully")
  void createMembershipPlan_returns204() throws Exception {
    var request = new MembershipPlan(null, "Premium Plan", 49.99, MembershipPlan.Recurring.MONTHLY, 1, false);

    when(productService.create(eq(gymId), any(MembershipPlan.class)))
      .thenReturn(new String[]{ "new_plan_id", "price" });

    mockMvc.perform(post("/v1/gyms/{gymId}/memberships/plans", gymId)
        .with(jwtAuth())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isNoContent());

    verify(productService).create(eq(gymId), any(MembershipPlan.class));
  }

  @Test
  @Order(5)
  @DisplayName("Returns all membership plans")
  void getMembershipPlans_returnsAllPlans() throws Exception {
    mockMvc.perform(get("/v1/gyms/{gymId}/memberships/plans", gymId)
        .with(jwtAuth())
        .queryParam("gymBranchId", "1")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(3))
      .andExpect(jsonPath("$[0].name").value("Basic"))
      .andExpect(jsonPath("$[0].price").value(29.99))
      .andExpect(jsonPath("$[0].recurring").value("MONTHLY"))
      .andExpect(jsonPath("$[1].name").value("Premium"))
      .andExpect(jsonPath("$[1].price").value(49.99))
      .andExpect(jsonPath("$[2].name").value("Premium Plan"))
      .andExpect(jsonPath("$[2].price").value(49.99));
  }

  @Test
  @Order(6)
  @DisplayName("Deletes a membership plan successfully")
  void deleteMembershipPlan_returns204() throws Exception {
    mockMvc.perform(delete("/v1/gyms/{gymId}/memberships/plans/{planId}", gymId, "new_plan_id")
        .with(jwtAuth())
        .queryParam("gymBranchId", "1")
      )
      .andExpect(status().isNoContent());

    assertThat(membershipPlanJpaRepository.findById("new_plan_id")).isEmpty();
    verify(productService).delete(1, "new_plan_id");
  }

  @Test
  @Order(7)
  @DisplayName("Returns members of a branch")
  void getBranchMembers_returnsMembers() throws Exception {
    mockMvc.perform(get("/v1/gyms/{gymId}/branches/{branchId}/members", gymId, 1)
        .with(jwtAuth())
        .queryParam("gymBranchId", "1")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].name").value("John"))
      .andExpect(jsonPath("$[0].first_surname").value("Doe"))
      .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
  }

}
