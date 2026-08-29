package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
@IdClass(RoutineDayExerciseEntity.ID.class)
public class RoutineDayExerciseEntity {

  @Id
  @Column(name = "exercise_id", nullable = false)
  private Integer exerciseId;

  @OnDelete(action = OnDeleteAction.CASCADE)
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "routine_id", referencedColumnName = "routine_id", nullable = false)
  @JoinColumn(name = "day_number", referencedColumnName = "day_number", nullable = false)
  private RoutineDayEntity day;

  @Column(name = "position", nullable = false)
  private Short position;

  @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RoutineDayExerciseSetEntity> sets = new ArrayList<>();

  @Column(name = "rest_seconds", precision = 5, scale = 1)
  private BigDecimal restSeconds;

  public void addSet(RoutineDayExerciseSetEntity set) {
    sets.add(set);
    set.setExercise(this);
  }


  @Getter
  @Setter
  @Embeddable
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = 942714046996821003L;

    private RoutineDayEntity day;

    private Integer exerciseId;

  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    RoutineDayExerciseEntity that = (RoutineDayExerciseEntity) o;
    return getDay() != null && Objects.equals(getDay(), that.getDay())
      && getExerciseId() != null && Objects.equals(getExerciseId(), that.getExerciseId());
  }


  @Override
  public final int hashCode() {
    return Objects.hash(day, exerciseId);
  }
}
