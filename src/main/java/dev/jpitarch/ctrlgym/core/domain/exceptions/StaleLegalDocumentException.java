package dev.jpitarch.ctrlgym.core.domain.exceptions;

import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;

public class StaleLegalDocumentException extends RuntimeException {
  public StaleLegalDocumentException(LegalDocumentType type) {
    super("Document type %s is not active".formatted(type));
  }
}
