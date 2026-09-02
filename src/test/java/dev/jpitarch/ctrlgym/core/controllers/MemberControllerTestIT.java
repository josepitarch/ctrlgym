package dev.jpitarch.ctrlgym.core.controllers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jpitarch.ctrlgym.core.dto.CreateMemberRequest;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.domain.enums.UserStatus;
import dev.jpitarch.ctrlgym.core.domain.enums.WorkoutStatus;
import dev.jpitarch.ctrlgym.core.entities.MembershipEntity;
import dev.jpitarch.ctrlgym.core.entities.RoutineEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.UserJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.WorkoutJpaRepository;
import dev.jpitarch.ctrlgym.payments.services.CustomerService;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberControllerTestIT extends BaseIntegrationTest {

  @MockitoBean
  SubscriptionService subscriptionService;

  @MockitoBean
  CustomerService customerService;

  @MockitoBean
  dev.jpitarch.ctrlgym.core.StripeBridge stripeBridge;

  @Autowired
  UserJpaRepository userJpaRepository;

  @Autowired
  MembershipJpaRepository membershipJpaRepository;

  @Autowired
  RoutineJpaRepository routineJpaRepository;

  @Autowired
  WorkoutJpaRepository workoutJpaRepository;

  JsonMapper jsonMapper = JsonMapper.builder()
    .addModule(new JavaTimeModule())
    .build();

  UUID memberId = UUID.fromString("c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1");
  UUID minorMemberId = UUID.fromString("d2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2");
  Integer gymId = 1;

  UUID termsOfServiceVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000001");
  UUID privacyPolicyVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000002");
  UUID staleTermsVersionId = UUID.fromString("d0d0d0d0-0000-0000-0000-000000000004");

  private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtAuth() {
    return jwt().jwt(j -> j.subject(memberId.toString()))
      .authorities(new SimpleGrantedAuthority("ROLE_MEMBER"));
  }

  private CreateMemberRequest buildBaseRequest() {
    var request = new CreateMemberRequest();
    request.setName("Test");
    request.setFirstSurname("Member");
    request.setSecondSurname("User");
    request.setGender(Gender.MALE);
    request.setBirthDate(LocalDate.of(1995, 8, 20));
    request.setNif("12345678Z");

    var address = new CreateMemberRequest.Address();
    address.setCity("Valencia");
    address.setPostalCode(46001);
    request.setAddress(address);

    return request;
  }

  @BeforeEach
  void resetMemberStatus() {
    userJpaRepository.findById(memberId).ifPresent(u -> {
      u.setStatus(UserStatus.AUTH);
      userJpaRepository.save(u);
    });
    userJpaRepository.findById(minorMemberId).ifPresent(u -> {
      u.setStatus(UserStatus.AUTH);
      userJpaRepository.save(u);
    });
    membershipJpaRepository.findByMemberId(memberId).forEach(membershipJpaRepository::delete);
    workoutJpaRepository.findByMemberId(memberId).forEach(workoutJpaRepository::delete);
    routineJpaRepository.findByMemberId(memberId).forEach(routineJpaRepository::delete);
  }

  @Nested
  @DisplayName("[CREATE-MEMBER]")
  @Tag("CREATE-MEMBER")
  class CreateMemberTests {

    @Test
    @Order(1)
    @DisplayName("Creates a member successfully when all mandatory documents are accepted")
    void createMember_withAllMandatoryDocuments_returns201() throws Exception {
      var request = buildBaseRequest();
      request.setAcceptedDocumentVersionIds(List.of(termsOfServiceVersionId, privacyPolicyVersionId));

      when(customerService.create(any())).thenReturn("cus_test_member");

      mockMvc.perform(post("/v1/members/{memberId}", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    @DisplayName("Returns 409 when not all mandatory documents are accepted")
    void createMember_missingMandatoryDocument_returns409() throws Exception {
      var request = buildBaseRequest();
      request.setAcceptedDocumentVersionIds(List.of(termsOfServiceVersionId));

      mockMvc.perform(post("/v1/members/{memberId}", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("Returns 409 when an accepted document version is not active")
    void createMember_withStaleDocument_returns409() throws Exception {
      var request = buildBaseRequest();
      request.setAcceptedDocumentVersionIds(List.of(staleTermsVersionId, privacyPolicyVersionId));

      mockMvc.perform(post("/v1/members/{memberId}", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
    }

    @Test
    @Order(4)
    @DisplayName("Creates a minor member with PENDING_GUARDIAN_CONSENT status and publishes event")
    void createMember_minor_returns201_withPendingGuardianConsent() throws Exception {
      var request = buildBaseRequest();
      request.setBirthDate(LocalDate.now().minusYears(16));
      request.setAcceptedDocumentVersionIds(List.of(termsOfServiceVersionId, privacyPolicyVersionId));

      when(customerService.create(any())).thenReturn("cus_test_minor");

      mockMvc.perform(post("/v1/members/{memberId}", minorMemberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwt().jwt(j -> j.subject(minorMemberId.toString()))
            .authorities(new SimpleGrantedAuthority("ROLE_MEMBER")))
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

      var user = userJpaRepository.findById(minorMemberId).orElseThrow();
      assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_GUARDIAN_CONSENT);
    }
  }

  @Nested
  @DisplayName("[INITIALIZE-MEMBERSHIP]")
  @Tag("INITIALIZE-MEMBERSHIP")
  class InitializeMembershipTests {

    @Test
    @Order(1)
    @DisplayName("Initializes membership successfully when member is ACTIVE with all Stripe data")
    void initializeMembership_success() throws Exception {
      var user = userJpaRepository.findById(memberId).orElseThrow();
      user.setStatus(UserStatus.ACTIVE);
      user.setStripeCustomerId("cus_test_member");
      user.setStripeSetupIntentId("seti_test_member");
      userJpaRepository.save(user);

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn("acct_test");
      when(stripeBridge.getStripePriceId("plan_basic")).thenReturn("price_basic123");
      when(stripeBridge.getStripeCustomerId(memberId)).thenReturn(java.util.Optional.of("cus_test_member"));
      when(stripeBridge.getStripeSetupIntentId(memberId)).thenReturn(java.util.Optional.of("seti_test_member"));
      when(subscriptionService.create(eq(memberId), eq(gymId), any(Map.class))).thenReturn("sub_test123");

      mockMvc.perform(post("/v1/members/{memberId}/memberships/{membershipPlanId}", memberId, "plan_basic")
          .header("X-Tenant-Id", gymId.toString())
          .param("gymId", gymId.toString())
          .with(jwtAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.recurring").value("MONTHLY"))
        .andExpect(jsonPath("$.next_billing_date").isNotEmpty());

      verify(subscriptionService).create(eq(memberId), eq(gymId), any(Map.class));
    }

    @Test
    @Order(2)
    @DisplayName("Returns 422 when member status is not ACTIVE")
    void initializeMembership_accountNotActivated() throws Exception {
      var user = userJpaRepository.findById(memberId).orElseThrow();
      user.setStatus(UserStatus.AUTH);
      user.setStripeCustomerId("cus_test_member");
      user.setStripeSetupIntentId("seti_test_member");
      userJpaRepository.save(user);

      mockMvc.perform(post("/v1/members/{memberId}/memberships/{membershipPlanId}", memberId, "plan_basic")
          .header("X-Tenant-Id", gymId.toString())
          .param("gymId", gymId.toString())
          .with(jwtAuth()))
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    @Order(3)
    @DisplayName("Returns 500 when member already has an active membership for the same plan")
    void initializeMembership_duplicateMembership() throws Exception {
      var user = userJpaRepository.findById(memberId).orElseThrow();
      user.setStatus(UserStatus.ACTIVE);
      user.setStripeCustomerId("cus_test_member");
      user.setStripeSetupIntentId("seti_test_member");
      userJpaRepository.save(user);

      var existingMembership = new MembershipEntity();
      existingMembership.setMemberId(memberId);
      existingMembership.setGymId(gymId);
      existingMembership.setMembershipPlanId("plan_basic");
      existingMembership.setStartDate(LocalDate.now().minusMonths(1));
      existingMembership.setEndDate(null);
      existingMembership.setNextBillingDate(LocalDate.now().plusMonths(1));
      existingMembership.setAutoRenew(true);
      existingMembership.setStripeSubscriptionId("sub_existing");
      membershipJpaRepository.save(existingMembership);

      mockMvc.perform(post("/v1/members/{memberId}/memberships/{membershipPlanId}", memberId, "plan_basic")
          .header("X-Tenant-Id", gymId.toString())
          .param("gymId", gymId.toString())
          .with(jwtAuth()))
        .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(4)
    @DisplayName("Returns 422 when member has no Stripe customer ID")
    void initializeMembership_missingCustomerId() throws Exception {
      var user = userJpaRepository.findById(memberId).orElseThrow();
      user.setStatus(UserStatus.ACTIVE);
      user.setStripeCustomerId(null);
      user.setStripeSetupIntentId("seti_test_member");
      userJpaRepository.save(user);

      mockMvc.perform(post("/v1/members/{memberId}/memberships/{membershipPlanId}", memberId, "plan_basic")
          .header("X-Tenant-Id", gymId.toString())
          .param("gymId", gymId.toString())
          .with(jwtAuth()))
        .andExpect(status().isUnprocessableContent());
    }

    @Test
    @Order(5)
    @DisplayName("Returns 422 when member has no Stripe setup intent ID")
    void initializeMembership_missingSetupIntentId() throws Exception {
      var user = userJpaRepository.findById(memberId).orElseThrow();
      user.setStatus(UserStatus.ACTIVE);
      user.setStripeCustomerId("cus_test_member");
      user.setStripeSetupIntentId(null);
      userJpaRepository.save(user);

      mockMvc.perform(post("/v1/members/{memberId}/memberships/{membershipPlanId}", memberId, "plan_basic")
          .header("X-Tenant-Id", gymId.toString())
          .param("gymId", gymId.toString())
          .with(jwtAuth()))
        .andExpect(status().isUnprocessableContent());
    }
  }

  @Nested
  @DisplayName("[CHANGE-MEMBERSHIP]")
  @Tag("CHANGE-MEMBERSHIP")
  class ChangeMembershipTests {

    @Test
    @Order(1)
    @DisplayName("Changes membership plan successfully")
    void changeMembership_success() throws Exception {
      var user = userJpaRepository.findById(memberId).orElseThrow();
      user.setStatus(UserStatus.ACTIVE);
      userJpaRepository.save(user);

      var existingMembership = new MembershipEntity();
      existingMembership.setMemberId(memberId);
      existingMembership.setGymId(gymId);
      existingMembership.setMembershipPlanId("plan_basic");
      existingMembership.setStartDate(LocalDate.now().minusMonths(1));
      existingMembership.setEndDate(null);
      existingMembership.setNextBillingDate(LocalDate.now().plusMonths(1));
      existingMembership.setAutoRenew(true);
      existingMembership.setStripeSubscriptionId("sub_change_test");
      membershipJpaRepository.save(existingMembership);

      when(stripeBridge.getStripeSubscriptionId(existingMembership.getId())).thenReturn("sub_change_test");
      when(stripeBridge.getStripeAccountId(gymId)).thenReturn("acct_test");
      when(stripeBridge.getStripePriceId(any())).thenReturn("price_basic123").thenReturn("price_premium456");

      mockMvc.perform(put("/v1/members/{memberId}/memberships", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .param("gymId", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content("\"plan_premium\""))
        .andExpect(status().isNoContent());

      verify(subscriptionService).change(eq("sub_change_test"), eq("price_basic123"), eq("price_premium456"), eq("acct_test"));
    }

    @Test
    @Order(2)
    @DisplayName("Returns 500 when changing membership without active membership")
    void changeMembership_noActiveMembership() throws Exception {
      membershipJpaRepository.findByMemberId(memberId).forEach(membershipJpaRepository::delete);

      mockMvc.perform(put("/v1/members/{memberId}/memberships", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .param("gymId", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content("\"plan_premium\""))
        .andExpect(status().isInternalServerError());
    }
  }

  @Nested
  @DisplayName("[CANCEL-MEMBERSHIP]")
  @Tag("CANCEL-MEMBERSHIP")
  class CancelMembershipTests {

    @Test
    @Order(1)
    @DisplayName("Cancels membership successfully")
    void cancelMembership_success() throws Exception {
      var user = userJpaRepository.findById(memberId).orElseThrow();
      user.setStatus(UserStatus.ACTIVE);
      userJpaRepository.save(user);

      var existingMembership = new MembershipEntity();
      existingMembership.setMemberId(memberId);
      existingMembership.setGymId(gymId);
      existingMembership.setMembershipPlanId("plan_basic");
      existingMembership.setStartDate(LocalDate.now().minusMonths(1));
      existingMembership.setEndDate(null);
      existingMembership.setNextBillingDate(LocalDate.now().plusMonths(1));
      existingMembership.setAutoRenew(true);
      existingMembership.setStripeSubscriptionId("sub_cancel_test");
      membershipJpaRepository.save(existingMembership);

      when(stripeBridge.getStripeAccountId(gymId)).thenReturn("acct_test");
      when(stripeBridge.getStripeSubscriptionId(existingMembership.getId())).thenReturn("sub_cancel_test");
      when(subscriptionService.cancel(any(Map.class))).thenReturn(LocalDate.now().plusDays(15));

      var body = Map.of("comment", "Moving to another city");

      mockMvc.perform(patch("/v1/members/{memberId}/memberships/{membershipId}", memberId, existingMembership.getId())
          .header("X-Tenant-Id", gymId.toString())
          .param("gymId", gymId.toString())
          .param("cancellationReasonId", "3")
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(body)))
        .andExpect(status().isNoContent());

      verify(subscriptionService).cancel(any(Map.class));

      var updatedMembership = membershipJpaRepository.findById(existingMembership.getId()).orElseThrow();
      assertThat(updatedMembership.getEndDate()).isNotNull();
      assertThat(updatedMembership.getCancellationReasonId()).isEqualTo(3);
      assertThat(updatedMembership.getCancellationComment()).isEqualTo("Moving to another city");
    }

    @Test
    @Order(2)
    @DisplayName("Returns 204 when cancelling membership with non-existent membershipId (no-op)")
    void cancelMembership_nonExistentMembershipId() throws Exception {
      when(stripeBridge.getStripeAccountId(gymId)).thenReturn("acct_test");
      when(stripeBridge.getStripeSubscriptionId(99999L)).thenReturn("sub_non_existent");
      when(subscriptionService.cancel(any(Map.class))).thenReturn(LocalDate.now().plusDays(15));

      var body = Map.of("comment", "Test comment");

      mockMvc.perform(patch("/v1/members/{memberId}/memberships/{membershipId}", memberId, 99999L)
          .header("X-Tenant-Id", gymId.toString())
          .param("gymId", gymId.toString())
          .param("cancellationReasonId", "1")
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(body)))
        .andExpect(status().isNoContent());

      verify(subscriptionService).cancel(any(Map.class));
    }
  }

  @Nested
  @DisplayName("[ROUTINE]")
  @Tag("ROUTINE")
  class RoutineTests {

    @Test
    @Order(1)
    @DisplayName("Creates a routine successfully")
    void createRoutine_returns201() throws Exception {
      var routineJson = jsonMapper.readTree(new ClassPathResource("fixtures/routine_push_pull_legs.json").getInputStream()).toString();

      mockMvc.perform(post("/v1/members/{memberId}/routines", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(routineJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Push Pull Legs"))
        .andExpect(jsonPath("$.days.length()").value(2))
        .andExpect(jsonPath("$.days[0].day_number").value(1))
        .andExpect(jsonPath("$.days[0].name").value("Push"))
        .andExpect(jsonPath("$.days[0].description").value("Chest, shoulders and triceps"))
        .andExpect(jsonPath("$.days[0].exercises.length()").value(2))
        .andExpect(jsonPath("$.days[0].exercises[0].id").value(1))
        .andExpect(jsonPath("$.days[0].exercises[0].name").value("Press de banca"))
        .andExpect(jsonPath("$.days[0].exercises[0].muscle_group").value("CHEST"))
        .andExpect(jsonPath("$.days[0].exercises[0].position").value(1))
        .andExpect(jsonPath("$.days[0].exercises[0].sets.length()").value(3))
        .andExpect(jsonPath("$.days[0].exercises[0].sets[0].number").value(1))
        .andExpect(jsonPath("$.days[0].exercises[0].sets[0].repetition").value(10))
        .andExpect(jsonPath("$.days[0].exercises[0].sets[1].number").value(2))
        .andExpect(jsonPath("$.days[0].exercises[0].sets[1].repetition").value(8))
        .andExpect(jsonPath("$.days[0].exercises[0].sets[2].number").value(3))
        .andExpect(jsonPath("$.days[0].exercises[0].sets[2].repetition").value(8))
        .andExpect(jsonPath("$.days[0].exercises[1].id").value(8))
        .andExpect(jsonPath("$.days[0].exercises[1].name").value("Press inclinado con mancuernas"))
        .andExpect(jsonPath("$.days[0].exercises[1].muscle_group").value("CHEST"))
        .andExpect(jsonPath("$.days[0].exercises[1].position").value(2))
        .andExpect(jsonPath("$.days[0].exercises[1].sets.length()").value(2))
        .andExpect(jsonPath("$.days[0].exercises[1].sets[0].number").value(1))
        .andExpect(jsonPath("$.days[0].exercises[1].sets[0].repetition").value(10))
        .andExpect(jsonPath("$.days[0].exercises[1].sets[1].number").value(2))
        .andExpect(jsonPath("$.days[0].exercises[1].sets[1].repetition").value(10))
        .andExpect(jsonPath("$.days[1].day_number").value(2))
        .andExpect(jsonPath("$.days[1].name").value("Pull"))
        .andExpect(jsonPath("$.days[1].description").value("Back and biceps"))
        .andExpect(jsonPath("$.days[1].exercises.length()").value(1))
        .andExpect(jsonPath("$.days[1].exercises[0].id").value(2))
        .andExpect(jsonPath("$.days[1].exercises[0].name").value("Dominadas"))
        .andExpect(jsonPath("$.days[1].exercises[0].muscle_group").value("BACK"))
        .andExpect(jsonPath("$.days[1].exercises[0].position").value(1))
        .andExpect(jsonPath("$.days[1].exercises[0].sets.length()").value(3))
        .andExpect(jsonPath("$.days[1].exercises[0].sets[0].number").value(1))
        .andExpect(jsonPath("$.days[1].exercises[0].sets[0].repetition").value(8))
        .andExpect(jsonPath("$.days[1].exercises[0].sets[1].number").value(2))
        .andExpect(jsonPath("$.days[1].exercises[0].sets[1].repetition").value(8))
        .andExpect(jsonPath("$.days[1].exercises[0].sets[2].number").value(3))
        .andExpect(jsonPath("$.days[1].exercises[0].sets[2].repetition").value(6));
    }

    @Test
    @Order(2)
    @DisplayName("Returns member routines")
    void getRoutines_returnsRoutines() throws Exception {
      var routineJson = jsonMapper.readTree(new ClassPathResource("fixtures/routine_push_pull_legs.json").getInputStream()).toString();

      mockMvc.perform(post("/v1/members/{memberId}/routines", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(routineJson))
        .andExpect(status().isCreated());

      mockMvc.perform(get("/v1/members/{memberId}/routines", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Push Pull Legs"))
        .andExpect(jsonPath("$[0].days.length()").value(2))
        .andExpect(jsonPath("$[0].days[0].day_number").value(1))
        .andExpect(jsonPath("$[0].days[0].name").value("Push"))
        .andExpect(jsonPath("$[0].days[0].description").value("Chest, shoulders and triceps"))
        .andExpect(jsonPath("$[0].days[0].exercises.length()").value(2))
        .andExpect(jsonPath("$[0].days[0].exercises[0].id").value(1))
        .andExpect(jsonPath("$[0].days[0].exercises[0].name").value("Press de banca"))
        .andExpect(jsonPath("$[0].days[0].exercises[0].muscle_group").value("CHEST"))
        .andExpect(jsonPath("$[0].days[0].exercises[0].position").value(1))
        .andExpect(jsonPath("$[0].days[0].exercises[0].sets.length()").value(3))
        .andExpect(jsonPath("$[0].days[0].exercises[0].sets[0].number").value(1))
        .andExpect(jsonPath("$[0].days[0].exercises[0].sets[0].repetition").value(10))
        .andExpect(jsonPath("$[0].days[0].exercises[0].sets[1].number").value(2))
        .andExpect(jsonPath("$[0].days[0].exercises[0].sets[1].repetition").value(8))
        .andExpect(jsonPath("$[0].days[0].exercises[0].sets[2].number").value(3))
        .andExpect(jsonPath("$[0].days[0].exercises[0].sets[2].repetition").value(8))
        .andExpect(jsonPath("$[0].days[0].exercises[1].id").value(8))
        .andExpect(jsonPath("$[0].days[0].exercises[1].name").value("Press inclinado con mancuernas"))
        .andExpect(jsonPath("$[0].days[0].exercises[1].muscle_group").value("CHEST"))
        .andExpect(jsonPath("$[0].days[0].exercises[1].position").value(2))
        .andExpect(jsonPath("$[0].days[0].exercises[1].sets.length()").value(2))
        .andExpect(jsonPath("$[0].days[0].exercises[1].sets[0].number").value(1))
        .andExpect(jsonPath("$[0].days[0].exercises[1].sets[0].repetition").value(10))
        .andExpect(jsonPath("$[0].days[0].exercises[1].sets[1].number").value(2))
        .andExpect(jsonPath("$[0].days[0].exercises[1].sets[1].repetition").value(10))
        .andExpect(jsonPath("$[0].days[1].day_number").value(2))
        .andExpect(jsonPath("$[0].days[1].name").value("Pull"))
        .andExpect(jsonPath("$[0].days[1].description").value("Back and biceps"))
        .andExpect(jsonPath("$[0].days[1].exercises.length()").value(1))
        .andExpect(jsonPath("$[0].days[1].exercises[0].id").value(2))
        .andExpect(jsonPath("$[0].days[1].exercises[0].name").value("Dominadas"))
        .andExpect(jsonPath("$[0].days[1].exercises[0].muscle_group").value("BACK"))
        .andExpect(jsonPath("$[0].days[1].exercises[0].position").value(1))
        .andExpect(jsonPath("$[0].days[1].exercises[0].sets.length()").value(3))
        .andExpect(jsonPath("$[0].days[1].exercises[0].sets[0].number").value(1))
        .andExpect(jsonPath("$[0].days[1].exercises[0].sets[0].repetition").value(8))
        .andExpect(jsonPath("$[0].days[1].exercises[0].sets[1].number").value(2))
        .andExpect(jsonPath("$[0].days[1].exercises[0].sets[1].repetition").value(8))
        .andExpect(jsonPath("$[0].days[1].exercises[0].sets[2].number").value(3))
        .andExpect(jsonPath("$[0].days[1].exercises[0].sets[2].repetition").value(6));
    }

    @Test
    @Order(3)
    @DisplayName("Deletes a routine successfully")
    void deleteRoutine_returns204() throws Exception {
      var routineJson = jsonMapper.readTree(new ClassPathResource("fixtures/routine_push_pull_legs.json").getInputStream()).toString();

      mockMvc.perform(post("/v1/members/{memberId}/routines", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(routineJson))
        .andExpect(status().isCreated());

      RoutineEntity routine = routineJpaRepository.findByMemberId(memberId).getFirst();

      mockMvc.perform(delete("/v1/members/{memberId}/routines/{routineId}", memberId, routine.getId())
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth()))
        .andExpect(status().isNoContent());

      assertThat(routineJpaRepository.findByMemberId(memberId)).isEmpty();
    }
  }

  @Nested
  @DisplayName("[WORKOUT]")
  @Tag("WORKOUT")
  class WorkoutTests {

    private Integer createRoutineAndGetId() throws Exception {
      var routineJson = jsonMapper.readTree(new ClassPathResource("fixtures/routine_push_pull_legs.json").getInputStream()).toString();

      var result = mockMvc.perform(post("/v1/members/{memberId}/routines", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(routineJson))
        .andExpect(status().isCreated())
        .andReturn();

      var responseJson = result.getResponse().getContentAsString();
      var routine = jsonMapper.readTree(responseJson);
      return routine.get("id").asInt();
    }

    @Test
    @Order(1)
    @DisplayName("Creates a workout successfully")
    void createWorkout_returns201() throws Exception {
      Integer routineId = createRoutineAndGetId();

      var workout = Map.of(
        "routine_id", routineId,
        "day_number", 1,
        "started_at", "2026-09-02T10:00:00Z",
        "finished_at", "2026-09-02T11:30:00Z",
        "status", "COMPLETED",
        "exercises", List.of(
          Map.of(
            "id", 1,
            "sets", List.of(
              Map.of("set_number", 1, "reps", 10, "weight", 60.0),
              Map.of("set_number", 2, "reps", 8, "weight", 65.0),
              Map.of("set_number", 3, "reps", 8, "weight", 65.0)
            )
          ),
          Map.of(
            "id", 8,
            "sets", List.of(
              Map.of("set_number", 1, "reps", 10, "weight", 20.0),
              Map.of("set_number", 2, "reps", 10, "weight", 22.5)
            )
          )
        )
      );

      mockMvc.perform(post("/v1/members/{memberId}/workouts", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(workout)))
        .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    @DisplayName("Returns member workouts paginated")
    void getWorkouts_returnsPaginatedWorkouts() throws Exception {
      Integer routineId = createRoutineAndGetId();

      var workout = Map.of(
        "routine_id", routineId,
        "day_number", 1,
        "started_at", "2026-09-02T10:00:00Z",
        "finished_at", "2026-09-02T11:30:00Z",
        "status", "COMPLETED",
        "exercises", List.of(
          Map.of(
            "id", 1,
            "sets", List.of(
              Map.of("set_number", 1, "reps", 10, "weight", 60.0),
              Map.of("set_number", 2, "reps", 8, "weight", 65.0)
            )
          )
        )
      );

      mockMvc.perform(post("/v1/members/{memberId}/workouts", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(workout)))
        .andExpect(status().isCreated());

      mockMvc.perform(get("/v1/members/{memberId}/workouts", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .param("page", "0")
          .param("size", "10")
          .with(jwtAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].routine_id").value(routineId))
        .andExpect(jsonPath("$.content[0].day_number").value(1))
        .andExpect(jsonPath("$.content[0].started_at").value("2026-09-02T10:00:00Z"))
        .andExpect(jsonPath("$.content[0].finished_at").value("2026-09-02T11:30:00Z"))
        .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
        .andExpect(jsonPath("$.content[0].exercises.length()").value(1))
        .andExpect(jsonPath("$.content[0].exercises[0].id").value(1))
        .andExpect(jsonPath("$.content[0].exercises[0].sets.length()").value(2))
        .andExpect(jsonPath("$.content[0].exercises[0].sets[0].set_number").value(1))
        .andExpect(jsonPath("$.content[0].exercises[0].sets[0].reps").value(10))
        .andExpect(jsonPath("$.content[0].exercises[0].sets[1].set_number").value(2))
        .andExpect(jsonPath("$.content[0].exercises[0].sets[1].reps").value(8))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @Order(3)
    @DisplayName("Creates workout with IN_PROGRESS status")
    void createWorkout_inProgress_returns201() throws Exception {
      Integer routineId = createRoutineAndGetId();

      var workout = Map.of(
        "routine_id", routineId,
        "day_number", 2,
        "started_at", "2026-09-02T14:00:00Z",
        "status", "IN_PROGRESS",
        "exercises", List.of(
          Map.of(
            "id", 2,
            "sets", List.of(
              Map.of("set_number", 1, "reps", 8, "weight", 0.0)
            )
          )
        )
      );

      mockMvc.perform(post("/v1/members/{memberId}/workouts", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .with(jwtAuth())
          .contentType(MediaType.APPLICATION_JSON)
          .content(jsonMapper.writeValueAsString(workout)))
        .andExpect(status().isCreated());
    }

    @Test
    @Order(4)
    @DisplayName("Returns empty page when member has no workouts")
    void getWorkouts_empty_returnsEmptyPage() throws Exception {
      mockMvc.perform(get("/v1/members/{memberId}/workouts", memberId)
          .header("X-Tenant-Id", gymId.toString())
          .param("page", "0")
          .param("size", "10")
          .with(jwtAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.totalPages").value(0));
    }
  }
}
