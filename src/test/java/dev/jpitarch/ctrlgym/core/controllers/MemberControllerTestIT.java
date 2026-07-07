package dev.jpitarch.ctrlgym.core.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberControllerTestIT extends BaseIntegrationTest {

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
  void getMember_returns500_whenMemberNotFound() throws Exception {
    var nonExistentId = UUID.randomUUID();

    mockMvc.perform(get("/v1/members/{memberId}", nonExistentId)
                    .param("gymId", gymId.toString())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }
}
