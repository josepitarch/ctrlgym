package dev.jpitarch.ctrlgym.core.domain;

import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalDocumentVersion {

  private UUID id;

  private Integer gymId;

  private LegalDocumentType type;

  private String version;

  private String content;

  private String contentHash;

  private LocalDate effectiveDate;

  private boolean active;
}
