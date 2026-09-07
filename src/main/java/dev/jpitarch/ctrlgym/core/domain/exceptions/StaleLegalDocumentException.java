package dev.jpitarch.ctrlgym.core.domain.exceptions;

import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;

public class StaleLegalDocumentException extends RuntimeException {

  public StaleLegalDocumentException(LegalDocumentType type) {
    super("The accepted version of document type " + type.name() + " is not active");
  }
}
