package dev.jpitarch.ctrlgym.verifactu.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ValidateNifRequest(
  @JsonProperty("nif") String nif,
  @JsonProperty("nombre") String name
) {}
