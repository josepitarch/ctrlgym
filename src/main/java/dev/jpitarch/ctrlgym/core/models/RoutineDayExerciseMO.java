package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "routine_day_exercises")
@IdClass(RoutineDayExerciseMO.ID.class)
public class RoutineDayExerciseMO {

  @Id
  @Column(name = "routine_id", nullable = false)
  private Integer routineId;

  @Id
  @Column(name = "day_number", nullable = false)
  private Integer dayNumber;

  @Id
  @Column(name = "exercise_id", nullable = false)
  private Integer exerciseId;

  @MapsId("id")
  @JoinColumns({
    @JoinColumn(name = "routine_id",
      referencedColumnName = "routine_id",
      nullable = false),
    @JoinColumn(name = "day_number",
      referencedColumnName = "day_number",
      nullable = false) })
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private RoutineDayMO routineDaysMO;

  @Column(name = "position", nullable = false)
  private Short position;

  @Column(name = "sets", nullable = false)
  private Short sets;

  @Column(name = "reps", nullable = false)
  private Short reps;

  @Column(name = "rest_seconds", precision = 5, scale = 1)
  private BigDecimal restSeconds;


  @Getter
  @Setter
  @Embeddable
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = 942714046996821003L;

    private Integer routineId;

    private Integer dayNumber;

    private Integer exerciseId;

  }

}
