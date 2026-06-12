package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "routine_day_exercises")
@IdClass(RoutineDayExerciseMO.ID.class)
public class RoutineDayExerciseMO {

  @Id
  @Column(name = "exercise_id", nullable = false)
  private Integer exerciseId;

  @JoinColumn(name = "routine_id", referencedColumnName = "routine_id", nullable = false)
  @JoinColumn(name = "day_number", referencedColumnName = "day_number", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private RoutineDayMO day;

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

    private RoutineDayMO day;

    private Integer exerciseId;

  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    RoutineDayExerciseMO that = (RoutineDayExerciseMO) o;
    return getDay().getRoutine().getId() != null && Objects.equals(getDay().getRoutine().getId(), that.getDay().getRoutine().getId())
      && getDay().getDayNumber() != null && Objects.equals(getDay().getDayNumber(), that.getDay().getDayNumber())
      && getExerciseId() != null && Objects.equals(getExerciseId(), that.getExerciseId());
  }


  @Override
  public final int hashCode() {
    return Objects.hash(day.getRoutine().getId(), day.getDayNumber(), exerciseId);
  }
}
