package dev.jpitarch.ctrlgym.core.entities;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "routine_days")
@IdClass(RoutineDayEntity.ID.class)
public class RoutineDayEntity {

  @Id
  @Column(name = "day_number", nullable = false)
  private Short dayNumber;

  @Id
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "routine_id", nullable = false)
  private RoutineEntity routine;

  @Column(name = "name", length = 100)
  private String name;

  @Column(name = "description", length = 500)
  private String description;

  @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RoutineDayExerciseEntity> exercises = new ArrayList<>();

  public void addExercise(RoutineDayExerciseEntity exercise) {
    exercises.add(exercise);
    exercise.setDay(this);
  }


  @Getter
  @Setter
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = -1302489486342450890L;

    private Integer routine;

    private Short dayNumber;

  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    RoutineDayEntity that = (RoutineDayEntity) o;
    return getRoutine() != null && Objects.equals(getRoutine(), that.getRoutine())
      && getDayNumber() != null && Objects.equals(getDayNumber(), that.getDayNumber());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(routine, dayNumber);
  }
}
