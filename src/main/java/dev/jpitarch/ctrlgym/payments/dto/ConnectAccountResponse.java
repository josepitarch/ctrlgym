package dev.jpitarch.ctrlgym.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectAccountResponse {

  private String accountId;

  private String onboardingUrl;
}
