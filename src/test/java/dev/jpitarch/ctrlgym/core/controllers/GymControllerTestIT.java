package dev.jpitarch.ctrlgym.core.controllers;

import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GymControllerTestIT extends BaseIntegrationTest {

  @MockitoBean
  SubscriptionService subscriptionService;

  @Test
  void getExercises_returnsAllExercises() throws Exception {
    mockMvc.perform(get("/v1/gyms/{gymId}/exercises", 1)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(20))
            .andExpect(jsonPath("$[0].name").value("Press de banca"))
            .andExpect(jsonPath("$[0].muscle_group").value("CHEST"));
  }
}
