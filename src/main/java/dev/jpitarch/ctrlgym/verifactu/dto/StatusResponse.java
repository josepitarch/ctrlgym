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
public class StatusResponse {

  private String nif;

  private String serie;

  private String numero;

  @JsonProperty("fecha_expedicion")
  private String fechaExpedicion;

  private String operacion;

  private String estado;

  private String url;

  private String qr;

  @JsonProperty("codigo_error")
  private String codigoError;

  @JsonProperty("mensaje_error")
  private String mensajeError;

  @JsonProperty("estado_registro_duplicado")
  private String estadoRegistroDuplicado;

  @JsonProperty("huella")
  private String huella;
}