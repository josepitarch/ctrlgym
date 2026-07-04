package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "routine_day_exercise_sets")
@IdClass(RoutineDayExerciseSetMO.ID.class)
public class RoutineDayExerciseSetMO {

  @OnDelete(action = OnDeleteAction.CASCADE)
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "routine_id", referencedColumnName = "routine_id", nullable = false)
  @JoinColumn(name = "day_number", referencedColumnName = "day_number", nullable = false)
  @JoinColumn(name = "exercise_id", referencedColumnName = "exercise_id", nullable = false)
  private RoutineDayExerciseMO exercise;

  @Id
  @Column(name = "set", nullable = false)
  private Short set;

  @Column(name = "reps", nullable = false)
  private Short reps;

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    RoutineDayExerciseSetMO that = (RoutineDayExerciseSetMO) o;
    return getExercise() != null && Objects.equals(getExercise(), that.getExercise())
      && getSet() != null && Objects.equals(getSet(), that.getSet());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(exercise, set);
  }


  @Getter
  @Setter
  @Embeddable
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = -6827135681764457880L;

    private RoutineDayExerciseMO exercise;

    private Short set;

  }

}
