package dev.jpitarch.ctrlgym.core.models;

import dev.jpitarch.ctrlgym.core.domain.enums.MuscleGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.URI;

@Getter
@Setter
@Entity
@Table(name = "exercises")
public class ExerciseMO {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", length = Integer.MAX_VALUE)
  private String description;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "muscle_group", columnDefinition = "muscle_group not null")
  private MuscleGroup muscleGroup;

  @Column(name = "image")
  private String image;

  @Column(name = "gym_id")
  private Integer gymId;

}
