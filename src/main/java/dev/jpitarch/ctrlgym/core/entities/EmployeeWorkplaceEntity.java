package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "employee_workplace", indexes = {
  @Index(name = "employee_workplace_branch_fk", columnList = "gym_branch_id")
})
public class EmployeeWorkplaceEntity {

  @Id
  @Column(name = "employee_id")
  private UUID employeeId;

  @Column(name = "gym_id")
  private Integer gymId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gym_branch_id")
  private GymBranchEntity gymBranch;

  @Column(name = "all_branches", nullable = false)
  private Boolean allBranches;

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    EmployeeWorkplaceEntity that = (EmployeeWorkplaceEntity) o;
    return getEmployeeId() != null && Objects.equals(getEmployeeId(), that.getEmployeeId());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(employeeId);
  }

}
