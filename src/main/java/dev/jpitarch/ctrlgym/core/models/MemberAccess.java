package dev.jpitarch.ctrlgym.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "member_accesses")
public class MemberAccess {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "gym_branch_id")
  private Integer gymBranchId;

  @Column(name = "member_id")
  @JsonProperty("user_id")
  private UUID memberId;

  @Column(name = "direction")
  @JsonProperty("direction")
  private Integer direction;

  @Column(name = "created_at")
  @JsonProperty("created_at")
  private OffsetDateTime createdAt;

  @Column(name = "received_at")
  @JsonProperty("received_at")
  private OffsetDateTime receivedAt;
}
