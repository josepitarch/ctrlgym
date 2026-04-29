package dev.jpitarch.ctrlgym.models;

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
  private String deviceId;

  @Column(name = "event_ts", nullable = false)
  private OffsetDateTime eventTs;

  @ColumnDefault("now()")
  @Column(name = "received_ts", nullable = false)
  private OffsetDateTime receivedTs;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "hostname")
  private String hostname;

  @Column(name = "ip", length = 64)
  private String ip;

  @Column(name = "platform")
  private String platform;

  @Column(name = "python_version", length = 50)
  private String pythonVersion;

  @Column(name = "app_version", length = 50)
  private String appVersion;

  @Column(name = "uptime_seconds")
  private Long uptimeSeconds;

  @Column(name = "cpu_percent")
  private Double cpuPercent;

  @Column(name = "memory_percent")
  private Double memoryPercent;

  @Column(name = "disk_percent")
  private Double diskPercent;

  @Column(name = "temperature_c", precision = 5, scale = 2)
  private BigDecimal temperatureC;

  @Column(name = "sqlite_ok")
  private Boolean sqliteOk;

  @ColumnDefault("0")
  @Column(name = "pending_events")
  private Integer pendingEvents;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "raw_payload")
  private Map<String, Object> rawPayload;


}
