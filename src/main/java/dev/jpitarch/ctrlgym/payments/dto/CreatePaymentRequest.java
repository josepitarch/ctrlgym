package dev.jpitarch.ctrlgym.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

  private Long gymId;

  private Long memberId;

  private Long amount;

  private String currency;

  private String membershipId;

}
