package dev.jpitarch.ctrlgym.core.entities;

import dev.jpitarch.ctrlgym.core.domain.enums.GuardianConsentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "member_guardian_authorization")
public class MemberGuardianAuthorizationEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "member_id", nullable = false, unique = true)
  private UUID memberId;

  @Column(name = "guardian_first_name", nullable = false, length = 100)
  private String guardianFirstName;

  @Column(name = "guardian_last_name", nullable = false, length = 100)
  private String guardianLastName;

  @Column(name = "guardian_dni", nullable = false, length = 20)
  private String guardianDni;

  @Column(name = "guardian_email", nullable = false, length = 255)
  private String guardianEmail;

  @Enumerated(EnumType.STRING)
  @ColumnDefault("'PENDING'")
  @Column(name = "status", nullable = false)
  private GuardianConsentStatus status;

  @Column(name = "token", nullable = false, unique = true, length = 255)
  private String token;

  @Column(name = "token_expires_at", nullable = false)
  private OffsetDateTime tokenExpiresAt;

  @ColumnDefault("now()")
  @Column(name = "requested_at", nullable = false)
  private OffsetDateTime requestedAt;

  @Column(name = "approved_at")
  private OffsetDateTime approvedAt;

  @Column(name = "approved_ip", length = 45)
  private String approvedIp;

  @Column(name = "approved_user_agent", columnDefinition = "TEXT")
  private String approvedUserAgent;

  @ColumnDefault("false")
  @Column(name = "requires_accompaniment", nullable = false)
  private Boolean requiresAccompaniment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", insertable = false, updatable = false)
  private UserEntity member;
}
