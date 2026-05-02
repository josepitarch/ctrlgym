package dev.jpitarch.ctrlgym.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "device_heartbeat")
public class DeviceHeartbeat {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "device_id", nullable = false, length = 100)
  @JsonProperty("device_id")
  private String deviceId;

  @Column(name = "created_at", nullable = false)
  @JsonProperty("created_at")
  private OffsetDateTime createdAt;

  @Column(name = "received_at")
  @JsonProperty("received_at")
  private OffsetDateTime receivedAt;

  @Column(name = "hostname")
  private String hostname;

  @Column(name = "ip", length = 64)
  private String ip;

  @Column(name = "platform")
  private String platform;

  @Column(name = "python_version", length = 50)
  @JsonProperty("python_version")
  private String pythonVersion;

  @Column(name = "app_version", length = 50)
  @JsonProperty("app_version")
  private String appVersion;

  @Column(name = "cpu_percent")
  @JsonProperty("cpu_percent")
  private Double cpuPercent;

  @Column(name = "memory_percent")
  @JsonProperty("memory_percent")
  private Double memoryPercent;

  @Column(name = "disk_percent")
  @JsonProperty("disk_percent")
  private Double diskPercent;

  @Column(name = "temperature_c", precision = 5, scale = 2)
  @JsonProperty("temperature_c")
  private BigDecimal temperatureC;

  @Column(name = "sqlite_ok")
  @JsonProperty("sqlite_ok")
  private Boolean sqliteOk;

  @Column(name = "pending_events")
  @JsonProperty("pending_events")
  private Integer pendingEvents;

}
