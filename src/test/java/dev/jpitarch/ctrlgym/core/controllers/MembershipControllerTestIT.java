package dev.jpitarch.ctrlgym.core.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MembershipControllerTestIT extends BaseIntegrationTest {

  @Test
  @DisplayName("Returns all cancellation reasons")
  void getAllCancellationReasons_returnsAllReasons() throws Exception {
    mockMvc.perform(get("/v1/memberships/cancellation-reasons")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(16))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Precio demasiado alto"))
            .andExpect(jsonPath("$[0].description").value("La cuota o los servicios son demasiado caros"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("No utiliza el gimnasio"));
  }
}
