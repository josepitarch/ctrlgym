package dev.jpitarch.ctrlgym.core.models;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "routine_days")
@IdClass(RoutineDayMO.ID.class)
public class RoutineDayMO {

  @Id
  @Column(name = "routine_id", nullable = false)
  private Integer routineId;

  @Id
  @Column(name = "day_number", nullable = false)
  private Short dayNumber;

  @MapsId("routineId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "routine_id", nullable = false)
  private RoutineMO routineMO;

  @Column(name = "name", length = 100)
  private String name;

  @OneToMany(mappedBy = "routineDaysMO", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RoutineDayExerciseMO> exercises;

  @Getter
  @Setter
  @Embeddable
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = -1302489486342450890L;

    private Integer routineId;

    private Short dayNumber;

  }

}
