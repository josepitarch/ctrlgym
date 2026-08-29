package dev.jpitarch.ctrlgym.authentication.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenMO {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "gym_id", nullable = false)
  private Integer gymId;

  @Column(name = "token_hash", length = 64, nullable = false, unique = true)
  private String tokenHash;

  @ColumnDefault("false")
  @Column(name = "revoked", nullable = false)
  private boolean revoked;

  @Column(name = "replaced_by")
  private UUID replacedBy;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

}
