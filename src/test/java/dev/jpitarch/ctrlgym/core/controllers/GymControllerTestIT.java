package dev.jpitarch.ctrlgym.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jpitarch.ctrlgym.core.domain.Exercise;
import dev.jpitarch.ctrlgym.core.domain.enums.MuscleGroup;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ExerciseJpaRepository;
import dev.jpitarch.ctrlgym.payments.services.SubscriptionService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GymControllerTestIT extends BaseIntegrationTest {

  @MockitoBean
  SubscriptionService subscriptionService;

  @Autowired
  ExerciseJpaRepository exerciseJpaRepository;

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @Order(1)
  void createExercise_returns201() throws Exception {
    var exercise = Exercise.builder()
            .name("Curl de biceps")
            .description("Ejercicio para biceps")
            .muscleGroup(MuscleGroup.BICEPS)
            .image("https://example.com/curl.jpg")
            .build();

    mockMvc.perform(post("/v1/gyms/{gymId}/exercises", 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(exercise)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Curl de biceps"))
            .andExpect(jsonPath("$.muscle_group").value("BICEPS"));
  }

  @Test
  @Order(2)
  void getExercises_returnsAllExercises() throws Exception {
    mockMvc.perform(get("/v1/gyms/{gymId}/exercises", 1)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(21))
            .andExpect(jsonPath("$[0].name").value("Press de banca"))
            .andExpect(jsonPath("$[0].muscle_group").value("CHEST"));
  }



  @Test
  @Order(3)
  void deleteExercise_returns204() throws Exception {
    mockMvc.perform(delete("/v1/gyms/{gymId}/exercises/{exerciseId}", 1, 21))
            .andExpect(status().isNoContent());

    assertThat(exerciseJpaRepository.findById(21)).isEmpty();
  }

}
