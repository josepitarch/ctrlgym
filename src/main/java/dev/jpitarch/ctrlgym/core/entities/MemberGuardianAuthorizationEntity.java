package dev.jpitarch.ctrlgym.core.entities;

import dev.jpitarch.ctrlgym.core.domain.enums.GuardianConsentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Objects;
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

  @Column(name = "guardian_first_name", length = 100)
  private String guardianFirstName;

  @Column(name = "guardian_last_name", length = 100)
  private String guardianLastName;

  @Column(name = "guardian_dni", length = 20)
  private String guardianDni;

  @Column(name = "guardian_email")
  private String guardianEmail;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false)
  private GuardianConsentStatus status;

  @Column(name = "token", nullable = false, unique = true)
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

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    MemberGuardianAuthorizationEntity that = (MemberGuardianAuthorizationEntity) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
  }
}
