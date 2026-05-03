package dev.jpitarch.ctrlgym.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "access_events")
public class AccessEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "device_id")
  private String deviceId;

  @Column(name = "user_id")
  @JsonProperty("user_id")
  private String userId;

  @Column(name = "direction")
  @JsonProperty("direction")
  private Integer direction;

  @Column(name = "created_at")
  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @Column(name = "received_at")
  @JsonProperty("received_at")
  private LocalDateTime receivedAt;
}
