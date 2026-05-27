package dev.jpitarch.ctrlgym.verifactu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceResponse {

  private String uuid;
  private String estado;
  private String url;
  private String qr;

  @JsonProperty("huella")
  private String huella;
}
