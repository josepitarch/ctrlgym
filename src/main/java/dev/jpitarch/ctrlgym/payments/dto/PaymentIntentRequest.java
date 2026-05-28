package dev.jpitarch.ctrlgym.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentRequest {

  private String accountId;

  private Long amount;

  private String currency;

  private String customerEmail;

  private String membershipId;

  private String description;
}
