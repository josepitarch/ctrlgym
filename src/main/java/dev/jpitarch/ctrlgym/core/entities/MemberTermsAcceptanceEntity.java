package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "member_terms_acceptance")
public class MemberTermsAcceptanceEntity {
  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "member_id", nullable = false)
  private UUID memberId;

  @Column(name = "document_version_id", nullable = false)
  private UUID documentVersionId;

  @ColumnDefault("now()")
  @Column(name = "accepted_at", nullable = false)
  private OffsetDateTime acceptedAt;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  @Column(name = "revoked_at")
  private OffsetDateTime revokedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", insertable = false, updatable = false)
  private UserEntity member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_version_id", insertable = false, updatable = false)
  private LegalDocumentVersionEntity documentVersion;
}
