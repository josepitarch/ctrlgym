package dev.jpitarch.ctrlgym.core.domain.exceptions;

import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;

public class MissingMandatoryAcceptanceException extends RuntimeException {

  public MissingMandatoryAcceptanceException(LegalDocumentType mandatory) {
    super("Missing mandatory document acceptance: " + mandatory.name());
  }
}
