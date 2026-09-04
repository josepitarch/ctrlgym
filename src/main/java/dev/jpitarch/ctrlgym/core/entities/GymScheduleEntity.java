package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "gym_schedule")
@IdClass(GymScheduleEntity.ID.class)
@EqualsAndHashCode(of = {"gymId", "dayOfWeek"})
public class GymScheduleEntity {

  @Id
  @Column(name = "gym_id", nullable = false)
  private Integer gymId;

  @Id
  @Column(name = "day_of_week", nullable = false)
  private Integer dayOfWeek;

  @Column(name = "opens_at", nullable = false)
  private LocalTime opensAt;

  @Column(name = "closes_at", nullable = false)
  private LocalTime closesAt;

  @Getter
  @Setter
  @EqualsAndHashCode(of = {"gymId", "dayOfWeek"})
  public static class ID implements Serializable {

    private Integer gymId;

    private Integer dayOfWeek;

  }

}
