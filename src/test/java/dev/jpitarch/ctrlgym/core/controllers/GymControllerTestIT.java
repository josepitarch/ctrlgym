package dev.jpitarch.ctrlgym.core.controllers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.domain.MembershipPlan;
import dev.jpitarch.ctrlgym.core.domain.enums.MuscleGroup;
import dev.jpitarch.ctrlgym.core.domain.enums.RecurrenceType;
import dev.jpitarch.ctrlgym.core.dto.CreateOrderRequest;
import dev.jpitarch.ctrlgym.core.dto.CreateShiftRequest;
import dev.jpitarch.ctrlgym.core.dto.CreateShiftSeriesRequest;
import dev.jpitarch.ctrlgym.core.dto.UpdateShiftRequest;
import dev.jpitarch.ctrlgym.core.entities.GymBranchEntity;
import dev.jpitarch.ctrlgym.core.entities.ProductEntity;
import dev.jpitarch.ctrlgym.core.events.OrderCreatedEvent;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExerciseJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipPlanJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.OrderJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ProductJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ShiftJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ShiftSeriesJpaRepository;
import dev.jpitarch.ctrlgym.core.security.CustomJwtAuthenticationToken;
import dev.jpitarch.ctrlgym.payments.services.ProductService;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
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

class GymControllerTestIT extends BaseIntegrationTest {

  static final List<OrderCreatedEvent> capturedEvents = new java.util.ArrayList<>();

  @Component
  static class TestOrderEventListener {
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
      capturedEvents.add(event);
    }
  }

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

  @Autowired
  OrderJpaRepository orderJpaRepository;

  @Autowired
  ProductJpaRepository productJpaRepository;

  JsonMapper objectMapper = JsonMapper.builder()
    .addModule(new JavaTimeModule())
    .build();

  Integer gymId = 1;
  UUID employeeId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
  UUID memberId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
  Integer branchId = 1;

  @BeforeEach
  void setUp() {
    capturedEvents.clear();
    orderJpaRepository.deleteAll();
    productJpaRepository.deleteAll();

    GymBranchEntity branch = new GymBranchEntity();
    branch.setId(branchId);

    ProductEntity product1 = new ProductEntity();
    product1.setGymId(gymId);
    product1.setGymBranch(branch);
    product1.setName("Protein Shake");
    product1.setPrice(new BigDecimal("3.50"));
    product1.setStock((short) 50);
    productJpaRepository.save(product1);

    ProductEntity product2 = new ProductEntity();
    product2.setGymId(gymId);
    product2.setGymBranch(branch);
    product2.setName("Energy Bar");
    product2.setPrice(new BigDecimal("2.00"));
    product2.setStock((short) 100);
    productJpaRepository.save(product2);
  }

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

  @Nested
  @DisplayName("[EXERCISE]")
  @Tag("EXERCISE")
  class ExerciseTests {

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
  }

  @Nested
  @DisplayName("[MEMBERSHIP-PLAN]")
  @Tag("MEMBERSHIP-PLAN")
  class MembershipPlanTests {

    @Test
    @Order(1)
    @DisplayName("Creates a membership plan successfully")
    void createMembershipPlan_returns204() throws Exception {
      var request = MembershipPlan.builder()
        .name("Premium Plan")
        .price(49.99)
        .billingPeriod(MembershipPlan.BillingPeriod.MONTHLY)
        .gymBranchId(1)
        .allBranches(false)
        .build();

      when(productService.create(eq(gymId), any(MembershipPlan.class))).thenReturn(new String[]{"new_plan_id", "price"});

      mockMvc.perform(post("/v1/gyms/{gymId}/memberships/plans", gymId)
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

      verify(productService).create(eq(gymId), any(MembershipPlan.class));
    }

    @Test
    @Order(2)
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
    @Order(3)
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
  }

  @Nested
  @DisplayName("[MEMBER]")
  @Tag("MEMBER")
  class MemberTests {

    @Test
    @Order(1)
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

  @Nested
  @DisplayName("[SCHEDULE]")
  @Tag("SCHEDULE")
  class ScheduleTests {

    @Test
    @Order(1)
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
    @Order(2)
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
    @Order(3)
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
    @Order(4)
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
    @Order(5)
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
    @Order(6)
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
    @Order(7)
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
    @Order(8)
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

  @Nested
  @DisplayName("[LEGAL-DOCUMENT]")
  @Tag("LEGAL-DOCUMENT")
  class LegalDocumentTests {

    @Test
    @Order(1)
    @DisplayName("Returns current active legal documents")
    void getActiveLegalDocuments_returnsDocuments() throws Exception {
      mockMvc.perform(get("/v1/gyms/{gymId}/legal/documents/current", gymId)
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].type").value("TERMS_OF_USE"))
        .andExpect(jsonPath("$[0].version").value("1.0"))
        .andExpect(jsonPath("$[0].content").value("Terms of service content"))
        .andExpect(jsonPath("$[0].effective_date").value("2026-01-01"))
        .andExpect(jsonPath("$[1].type").value("PRIVACY_POLICY"))
        .andExpect(jsonPath("$[2].type").value("IMAGE_CONSENT"));
    }
  }

  @Nested
  @DisplayName("[ORDER]")
  @Tag("ORDER")
  class OrderTests {

    @Test
    @Order(1)
    @DisplayName("Creates an order successfully and publishes OrderCreatedEvent")
    void createOrder_returns201() throws Exception {
      List<ProductEntity> products = productJpaRepository.findAll();
      Integer productId1 = products.stream().filter(p -> p.getName().equals("Protein Shake")).findFirst().orElseThrow().getId();
      Integer productId2 = products.stream().filter(p -> p.getName().equals("Energy Bar")).findFirst().orElseThrow().getId();

      CreateOrderRequest request = new CreateOrderRequest();
      request.setMemberId(memberId);

      CreateOrderRequest.Item item1 = new CreateOrderRequest.Item();
      item1.setProductId(productId1);
      item1.setQuantity(2);

      CreateOrderRequest.Item item2 = new CreateOrderRequest.Item();
      item2.setProductId(productId2);
      item2.setQuantity(3);

      request.setItems(List.of(item1, item2));

      mockMvc.perform(post("/v1/gyms/{gymId}/branches/{branchId}/orders", gymId, branchId)
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.member_id").value(memberId.toString()))
        .andExpect(jsonPath("$.gym_branch_id").value(branchId))
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.items[0].product_name_snapshot").isString())
        .andExpect(jsonPath("$.items[0].product_price_snapshot").isNumber())
        .andExpect(jsonPath("$.items[0].quantity").isNumber());

      assertThat(capturedEvents).hasSize(1);
      OrderCreatedEvent event = capturedEvents.getFirst();
      assertThat(event.getOrderId()).isNotNull();
      assertThat(event.getMemberId()).isEqualTo(memberId);
      assertThat(event.getGymBranchId()).isEqualTo(branchId);
      assertThat(event.getGymId()).isEqualTo(gymId);
      assertThat(event.getItemCount()).isEqualTo(2);
    }

    @Test
    @Order(2)
    @DisplayName("Returns orders for a branch")
    void getOrders_returnsOrders() throws Exception {
      List<ProductEntity> products = productJpaRepository.findAll();
      Integer productId = products.stream().filter(p -> p.getName().equals("Protein Shake")).findFirst().orElseThrow().getId();

      CreateOrderRequest request = new CreateOrderRequest();
      request.setMemberId(memberId);

      CreateOrderRequest.Item item = new CreateOrderRequest.Item();
      item.setProductId(productId);
      item.setQuantity(1);

      request.setItems(List.of(item));

      mockMvc.perform(post("/v1/gyms/{gymId}/branches/{branchId}/orders", gymId, branchId)
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

      mockMvc.perform(get("/v1/gyms/{gymId}/branches/{branchId}/orders", gymId, branchId)
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].member_id").value(memberId.toString()))
        .andExpect(jsonPath("$[0].gym_branch_id").value(branchId))
        .andExpect(jsonPath("$[0].items.length()").value(1))
        .andExpect(jsonPath("$[0].created_at").isNotEmpty());
    }
  }
}
