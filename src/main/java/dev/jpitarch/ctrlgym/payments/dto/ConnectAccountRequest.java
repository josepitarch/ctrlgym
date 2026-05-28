package dev.jpitarch.ctrlgym.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectAccountRequest {

    private String email;

    private String businessName;

    private String country;
}
