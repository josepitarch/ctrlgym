package dev.jpitarch.ctrlgym.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetupIntentResponse {

  private String id;

  @JsonProperty("client_secret")
  private String clientSecret;

}
