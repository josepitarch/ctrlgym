package dev.jpitarch.ctrlgym.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String stripePaymentIntentId;
    private Long amount;
    private String currency;
    private String status;
    private String clientSecret;
    private LocalDateTime createdAt;
}