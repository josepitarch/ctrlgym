package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.core.models.MemberMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.MemberJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MembersControllerGetMemberTest extends BaseIntegrationTest {

  @Autowired
  MemberJpaRepository memberJpaRepository;

  private UUID memberId;
  private Integer gymId = 1;

  @BeforeEach
  void setUp() {
    memberId = UUID.randomUUID();

    MemberMO member = new MemberMO();
    member.setId(memberId);
    member.setGymId(gymId);
    member.setName("John");
    member.setFirstSurname("Doe");
    member.setSecondSurname("Smith");
    member.setEmail("john.doe@example.com");
    member.setNif("12345678A");
    member.setGender("M");
    member.setBirthDate(LocalDate.of(1990, 5, 15));
    member.setStreet("123 Main St");
    member.setCity("Test City");
    member.setState("Test State");
    member.setPostalCode(12345);
    member.setCountry("Test Country");

    memberJpaRepository.save(member);
  }

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
  void getMember_returns500_whenMemberNotFound() throws Exception {
    UUID nonExistentId = UUID.randomUUID();

    mockMvc.perform(get("/v1/members/{memberId}", nonExistentId)
        .param("gymId", gymId.toString())
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isInternalServerError());
  }
}
