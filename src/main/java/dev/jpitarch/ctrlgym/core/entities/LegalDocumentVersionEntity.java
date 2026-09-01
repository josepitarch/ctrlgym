package dev.jpitarch.ctrlgym.core.entities;

import dev.jpitarch.ctrlgym.core.domain.enums.LegalDocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "legal_document_version")
public class LegalDocumentVersionEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "gym_id", nullable = false)
  private Integer gymId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "type", nullable = false)
  private LegalDocumentType type;

  @Column(name = "version", nullable = false, length = 50)
  private String version;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "content_hash", nullable = false, length = 64)
  private String contentHash;

  @Column(name = "effective_date", nullable = false)
  private LocalDate effectiveDate;

  @ColumnDefault("true")
  @Column(name = "active", nullable = false)
  private Boolean active;

  @ColumnDefault("now()")
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gym_id", insertable = false, updatable = false)
  private GymEntity gym;
}
