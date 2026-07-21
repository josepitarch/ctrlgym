package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "gym_branches")
public class GymBranchMO {
  @Id
  @Column(name = "id", nullable = false)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "gym_id", nullable = false)
  private GymMO gym;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @ColumnDefault("true")
  @Column(name = "is_active")
  private Boolean isActive;

  @Column(name = "capacity")
  private Short capacity;

  @Column(name = "peak_hour_start")
  private LocalTime peakHourStart;

  @Column(name = "peak_hour_end")
  private LocalTime peakHourEnd;

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  @Column(name = "api_key")
  private String apiKey;


}
