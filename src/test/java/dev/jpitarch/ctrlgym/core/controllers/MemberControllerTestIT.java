package dev.jpitarch.ctrlgym.core.controllers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.enums.Gender;
import dev.jpitarch.ctrlgym.core.models.MemberMO;
import dev.jpitarch.ctrlgym.core.models.MembershipMO;
import dev.jpitarch.ctrlgym.core.models.RoutineMO;
import dev.jpitarch.ctrlgym.core.repositories.MembersRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MembershipJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.RoutineJpaRepository;
import dev.jpitarch.ctrlgym.payments.services.CustomerService;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberControllerTestIT extends BaseIntegrationTest {

  @MockitoBean
  SubscriptionService subscriptionService;

  @MockitoBean
  CustomerService customerService;

  @Autowired
  MembersRepository membersRepository;

  @Autowired
  MemberJpaRepository memberJpaRepository;

  @Autowired
  MembershipJpaRepository membershipJpaRepository;

  @Autowired
  RoutineJpaRepository routineJpaRepository;

  JsonMapper jsonMapper = JsonMapper.builder()
    .addModule(new JavaTimeModule())
    .build();

  Member.Id memberId = Member.Id.of(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"), 1);

  @Test
  @Order(1)
  @DisplayName("Creates a new member successfully")
  void createMember_returns201() throws Exception {
    var member = Member.builder()
      .nif("12345678A")
      .name("John")
      .firstSurname("Doe")
      .secondSurname("Johnson")
      .email("john.doe@example.com")
      .gender(Gender.MALE)
      .birthDate(LocalDate.of(1990, 5, 15))
      .address(Member.Address.builder()
        .street("123 Main St")
        .city("Springfield")
        .state("IL")
        .postalCode(62701)
        .country("USA")
        .build())
      .build();

    when(customerService.create(any())).thenReturn("cus_test123");

    mockMvc.perform(post("/v1/members/{memberId}", memberId.memberId())
        .param("gymId", memberId.gymId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(member)))
      .andExpect(status().isCreated());

    memberJpaRepository
      .findById(new MemberMO.ID(memberId.memberId(), memberId.gymId()))
      .ifPresentOrElse(m -> assertThat(m.getStripeCustomerId()).isEqualTo("cus_test123"), () -> fail("Member not found"));
  }

  @Test
  @Order(2)
  @DisplayName("Returns an existing member")
  void getMember_returnsMember() throws Exception {
    mockMvc.perform(get("/v1/members/{memberId}", memberId.memberId())
        .param("gymId", memberId.gymId().toString())
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id.member_id").value(memberId.memberId().toString()))
      .andExpect(jsonPath("$.id.gym_id").value(memberId.gymId().toString()))
      .andExpect(jsonPath("$.name").value("John"))
      .andExpect(jsonPath("$.first_surname").value("Doe"))
      .andExpect(jsonPath("$.second_surname").value("Johnson"))
      .andExpect(jsonPath("$.email").value("john.doe@example.com"))
      .andExpect(jsonPath("$.gender").value("MALE"));
  }

  @Test
  @Order(3)
  @DisplayName("Returns 404 when member not found")
  void getMember_returns404_whenMemberNotFound() throws Exception {
    var nonExistentId = UUID.randomUUID();

    mockMvc.perform(get("/v1/members/{memberId}", nonExistentId)
        .param("gymId", memberId.gymId().toString())
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound());
  }

  @Test
  @Order(4)
  @DisplayName("Returns 409 when generating QR without active membership")
  void generateQr_returns409_whenMemberHasNoActiveMembership() throws Exception {
    mockMvc.perform(post("/v1/members/{memberId}/generate-qr", memberId.memberId())
        .param("gymId", memberId.gymId().toString()))
      .andExpect(status().isConflict());
  }

  @Test
  @Order(5)
  @DisplayName("Initializes membership successfully")
  void initializeMembership_returns204_whenSuccessful() throws Exception {
    when(subscriptionService.create(any(), any())).thenReturn("sub_test123");

    membersRepository.savePaymentMethodId("cus_test123", "pm_test123");

    mockMvc.perform(post("/v1/members/{memberId}/memberships/{membershipId}", memberId.memberId(), "plan_basic")
        .param("gymId", memberId.gymId().toString()))
      .andExpect(status().isNoContent());
  }

  @Test
  @Order(6)
  @DisplayName("Returns all memberships for a member")
  void getMemberships_returnsAllMemberships() throws Exception {
    mockMvc.perform(get("/v1/members/{memberId}/memberships", memberId.memberId())
        .param("gymId", memberId.gymId().toString()))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].interval").value("MONTHLY"))
      .andExpect(jsonPath("$[0].next_billing_date").isNotEmpty());
  }

  @Test
  @Order(7)
  @DisplayName("Cancels membership successfully")
  void cancelMembership_returns204() throws Exception {
    var body = """
      {}
      """;
    mockMvc.perform(patch("/v1/members/{memberId}/memberships/{membershipId}", memberId.memberId(), "plan_basic")
        .param("gymId", memberId.gymId().toString())
        .param("cancellationReasonId", "1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isNoContent());

    MembershipMO membership = membershipJpaRepository.findById(1L).orElseThrow();
    assertThat(membership.getEndDate()).isNotNull();
    assertThat(membership.getCancellationReasonId()).isEqualTo(1);
  }

  @Test
  @Order(8)
  @DisplayName("Creates a routine successfully")
  void createRoutine_returns201() throws Exception {
    var routineJson = jsonMapper.readTree(new ClassPathResource("fixtures/routine_push_pull_legs.json").getInputStream()).toString();

    mockMvc.perform(post("/v1/members/{memberId}/routines", memberId.memberId())
        .param("gymId", memberId.gymId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(routineJson))
      .andExpect(status().isCreated());
  }

  @Test
  @Order(9)
  @DisplayName("Returns member routines")
  void getRoutines_returnsRoutines() throws Exception {
    mockMvc.perform(get("/v1/members/{memberId}/routines", memberId.memberId())
        .param("gymId", memberId.gymId().toString()))
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
  @Order(10)
  @DisplayName("Deletes a routine successfully")
  void deleteRoutine_returns204() throws Exception {
    RoutineMO routine = routineJpaRepository.findByMemberIdAndGymId(memberId.memberId(), memberId.gymId()).getFirst();

    mockMvc.perform(delete("/v1/members/{memberId}/routines/{routineId}", memberId.memberId(), routine.getId())
        .param("gymId", memberId.gymId().toString()))
      .andExpect(status().isNoContent());

    assertThat(routineJpaRepository.findByMemberIdAndGymId(memberId.memberId(), memberId.gymId())).isEmpty();
  }

}
