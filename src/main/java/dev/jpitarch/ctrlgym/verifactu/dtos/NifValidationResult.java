package dev.jpitarch.ctrlgym.verifactu.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NifValidationResult {

  IDENTIFICADO("IDENTIFICADO"),
  NO_IDENTIFICADO("NO IDENTIFICADO"),
  NO_IDENTIFICADO_SIMILAR("NO IDENTIFICADO-SIMILAR"),
  IDENTIFICADO_BAJA("IDENTIFICADO-BAJA"),
  IDENTIFICADO_REVOCADO("IDENTIFICADO-REVOCADO"),
  ERROR("ERROR");

  private final String value;

  NifValidationResult(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static NifValidationResult fromValue(String value) {
    for (NifValidationResult result : values()) {
      if (result.value.equals(value)) {
        return result;
      }
    }
    throw new IllegalArgumentException("Unexpected value: " + value);
  }
}
