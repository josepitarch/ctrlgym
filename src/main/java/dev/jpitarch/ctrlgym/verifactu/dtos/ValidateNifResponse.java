package dev.jpitarch.ctrlgym.verifactu.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ValidateNifResponse(
  @JsonProperty("nif") String nif,
  @JsonProperty("nombre") String name,
  @JsonProperty("resultado") NifValidationResult result
) {}
