package dev.jpitarch.ctrlgym.core.controllers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.domain.enums.MuscleGroup;
import dev.jpitarch.ctrlgym.core.domain.enums.RecurrenceType;
import dev.jpitarch.ctrlgym.core.dto.CreateShiftRequest;
import dev.jpitarch.ctrlgym.core.dto.CreateShiftSeriesRequest;
import dev.jpitarch.ctrlgym.core.dto.UpdateShiftRequest;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExerciseJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipPlanJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ShiftJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ShiftSeriesJpaRepository;
import dev.jpitarch.ctrlgym.core.security.CustomJwtAuthenticationToken;
import dev.jpitarch.ctrlgym.payments.services.ProductService;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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

  @Autowired
  ShiftSeriesJpaRepository shiftSeriesJpaRepository;

  @Autowired
  ShiftJpaRepository shiftJpaRepository;

  JsonMapper objectMapper = JsonMapper.builder()
    .addModule(new JavaTimeModule())
    .build();

  Integer gymId = 1;
  UUID employeeId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

  private RequestPostProcessor jwtAuth() {
    Jwt jwt = Jwt.withTokenValue("token")
      .header("alg", "none")
      .claim("gym_id", gymId)
      .claim("role", "MANAGER")
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
      .build();

    mockMvc.perform(multipart("/v1/gyms/{gymId}/exercises", gymId)
        .file(new MockMultipartFile("exercise", "", "application/json", objectMapper.writeValueAsBytes(exercise)))
        .with(jwtAuth()))
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
      .thenReturn(new String[]{"new_plan_id", "price"});

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

  @Test
  @Order(8)
  @DisplayName("Creates a shift series with weekly recurrence")
  void createShiftSeries_returns201() throws Exception {
    var request = new CreateShiftSeriesRequest(
      employeeId,
      gymId,
      LocalTime.of(9, 0),
      LocalTime.of(17, 0),
      RecurrenceType.WEEKLY,
      1,
      List.of((short) 1, (short) 3, (short) 5),
      LocalDate.of(2026, 9, 1),
      LocalDate.of(2026, 9, 30)
    );

    mockMvc.perform(post("/v1/gyms/{gymId}/branches/{branchId}/schedule/series", gymId, 1)
        .with(jwtAuth())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.employee_id").value(employeeId.toString()))
      .andExpect(jsonPath("$.recurrence_type").value("WEEKLY"))
      .andExpect(jsonPath("$.interval_value").value(1))
      .andExpect(jsonPath("$.start_time").value("09:00:00"))
      .andExpect(jsonPath("$.end_time").value("17:00:00"));
  }

  @Test
  @Order(9)
  @DisplayName("Returns shift series for employee")
  void getShiftSeries_returnsSeries() throws Exception {
    mockMvc.perform(get("/v1/gyms/{gymId}/branches/{branchId}/schedule/series", gymId, 1)
        .with(jwtAuth())
        .queryParam("employeeId", employeeId.toString())
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].recurrence_type").value("WEEKLY"));
  }

  @Test
  @Order(10)
  @DisplayName("Creates a single shift")
  void createSingleShift_returns201() throws Exception {
    var request = new CreateShiftRequest(
      employeeId,
      gymId,
      LocalDate.of(2026, 10, 15),
      LocalTime.of(8, 0),
      LocalTime.of(12, 0)
    );

    mockMvc.perform(post("/v1/gyms/{gymId}/branches/{branchId}/schedule/shifts", gymId, 1)
        .with(jwtAuth())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.employee_id").value(employeeId.toString()))
      .andExpect(jsonPath("$.shift_date").value("2026-10-15"))
      .andExpect(jsonPath("$.start_time").value("08:00:00"))
      .andExpect(jsonPath("$.end_time").value("12:00:00"))
      .andExpect(jsonPath("$.status").value("SCHEDULED"));
  }

  @Test
  @Order(11)
  @DisplayName("Returns shifts by date range")
  void getShifts_byDateRange_returnsShifts() throws Exception {
    mockMvc.perform(get("/v1/gyms/{gymId}/branches/{branchId}/schedule/shifts", gymId, 1)
        .with(jwtAuth())
        .queryParam("employeeId", employeeId.toString())
        .queryParam("from", "2026-09-01")
        .queryParam("to", "2026-09-30")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").isNumber());
  }

  @Test
  @Order(12)
  @DisplayName("Returns all shifts for employee")
  void getShifts_all_returnsShifts() throws Exception {
    mockMvc.perform(get("/v1/gyms/{gymId}/branches/{branchId}/schedule/shifts", gymId, 1)
        .with(jwtAuth())
        .queryParam("employeeId", employeeId.toString())
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").isNumber());
  }

  @Test
  @Order(13)
  @DisplayName("Updates a shift creating an exception")
  void updateShift_returnsModifiedShift() throws Exception {
    MvcResult result = mockMvc.perform(get("/v1/gyms/{gymId}/branches/{branchId}/schedule/shifts", gymId, 1)
        .with(jwtAuth())
        .queryParam("employeeId", employeeId.toString())
        .queryParam("from", "2026-09-01")
        .queryParam("to", "2026-09-30")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andReturn();

    String json = result.getResponse().getContentAsString();
    var shifts = objectMapper.readValue(json, List.class);
    assertThat(shifts).isNotEmpty();

    Number shiftId = (Number) ((Map<?, ?>) shifts.getFirst()).get("id");

    var request = new UpdateShiftRequest(
      null,
      LocalTime.of(10, 0),
      LocalTime.of(18, 0)
    );

    mockMvc.perform(put("/v1/gyms/{gymId}/branches/{branchId}/schedule/shifts/{shiftId}", gymId, 1, shiftId.longValue())
        .with(jwtAuth())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.start_time").value("10:00:00"))
      .andExpect(jsonPath("$.end_time").value("18:00:00"))
      .andExpect(jsonPath("$.is_exception").value(true))
      .andExpect(jsonPath("$.status").value("MODIFIED"));
  }

  @Test
  @Order(14)
  @DisplayName("Deletes a single shift from series (marks as cancelled)")
  void deleteShift_returns204() throws Exception {
    MvcResult result = mockMvc.perform(get("/v1/gyms/{gymId}/branches/{branchId}/schedule/shifts", gymId, 1)
        .with(jwtAuth())
        .queryParam("employeeId", employeeId.toString())
        .queryParam("from", "2026-09-01")
        .queryParam("to", "2026-09-30")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andReturn();

    String json = result.getResponse().getContentAsString();
    var shifts = objectMapper.readValue(json, List.class);
    assertThat(shifts).isNotEmpty();

    Number shiftId = (Number) ((Map<?, ?>) shifts.getFirst()).get("id");

    mockMvc.perform(delete("/v1/gyms/{gymId}/branches/{branchId}/schedule/shifts/{shiftId}", gymId, 1, shiftId.longValue())
        .with(jwtAuth()))
      .andExpect(status().isNoContent());

    assertThat(shiftJpaRepository.findById(shiftId.longValue())).isPresent();
  }

  @Test
  @Order(15)
  @DisplayName("Deletes a shift series and all its shifts")
  void deleteShiftSeries_returns204() throws Exception {
    MvcResult result = mockMvc.perform(get("/v1/gyms/{gymId}/branches/{branchId}/schedule/series", gymId, 1)
        .with(jwtAuth())
        .queryParam("employeeId", employeeId.toString())
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andReturn();

    String json = result.getResponse().getContentAsString();
    var series = objectMapper.readValue(json, List.class);
    assertThat(series).isNotEmpty();

    Number seriesId = (Number) ((Map<?, ?>) series.getFirst()).get("id");

    mockMvc.perform(delete("/v1/gyms/{gymId}/branches/{branchId}/schedule/series/{seriesId}", gymId, 1, seriesId.longValue())
        .with(jwtAuth()))
      .andExpect(status().isNoContent());

    assertThat(shiftSeriesJpaRepository.findById(seriesId.longValue())).isEmpty();
  }

}
