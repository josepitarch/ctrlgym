package dev.jpitarch.ctrlgym.verifactu.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatusResponse(
  String nif,
  String serie,
  String numero,
  @JsonProperty("fecha_expedicion") String fechaExpedicion,
  String operacion,
  String estado,
  String url,
  String qr,
  @JsonProperty("codigo_error") String codigoError,
  @JsonProperty("mensaje_error") String mensajeError,
  @JsonProperty("estado_registro_duplicado") String estadoRegistroDuplicado,
  @JsonProperty("huella") String huella
) {}
