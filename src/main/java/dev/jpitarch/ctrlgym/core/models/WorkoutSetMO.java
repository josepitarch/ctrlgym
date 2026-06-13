package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "workout_sets")
@IdClass(WorkoutSetMO.ID.class)
public class WorkoutSetMO {

  @Id
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "workout_id", nullable = false)
  private WorkoutMO workout;

  @Id
  @Column(name = "exercise_id", nullable = false)
  private Integer exerciseId;

  @Id
  @Column(name = "set", nullable = false)
  private Short set;

  @Column(name = "reps", nullable = false)
  private Short reps;

  @Getter
  @Setter
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = 1043301312275583860L;

    private Integer workout;

    private Integer exerciseId;

    private Short set;

  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    WorkoutSetMO that = (WorkoutSetMO) o;
    return getWorkout() != null && Objects.equals(getWorkout(), that.getWorkout())
      && getExerciseId() != null && Objects.equals(getExerciseId(), that.getExerciseId())
      && getSet() != null && Objects.equals(getSet(), that.getSet());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(workout, exerciseId, set);
  }

}
