package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "routines")
@NamedEntityGraph(
  name = "RoutineMO.withAllRelations",
  attributeNodes = {
    @NamedAttributeNode(value = "days", subgraph = "days-with-exercises")
  },
  subgraphs = {
    @NamedSubgraph(name = "days-with-exercises", attributeNodes = {
      @NamedAttributeNode(value = "exercises", subgraph = "exercises-with-sets")
    }),
    @NamedSubgraph(name = "exercises-with-sets", attributeNodes = {
      @NamedAttributeNode("sets")
    })
  }
)
public class RoutineMO {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "member_id")
  private UUID memberId;

  @Column(name = "gym_id")
  private Integer gymId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", length = Integer.MAX_VALUE)
  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "deleted_at")
  private LocalDate deletedAt;

  @OnDelete(action = OnDeleteAction.CASCADE)
  @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RoutineDayMO> days = new ArrayList<>();

  public void addDay(RoutineDayMO day) {
    days.add(day);
    day.setRoutine(this);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    RoutineMO routineMO = (RoutineMO) o;
    return getId() != null && Objects.equals(getId(), routineMO.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
  }
}
