package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "gym_metrics_monthly")
@IdClass(GymMetricsMO.ID.class)
public class GymMetricsMO {

  @Id
  @Column(name = "gym_branch_id")
  private Integer gymBranch;

  @Id
  @Column(name = "year_month", nullable = false)
  private LocalDate yearMonth;

  @ColumnDefault("0")
  @Column(name = "revenue", nullable = false, precision = 12, scale = 2)
  private BigDecimal revenue;

  @ColumnDefault("0")
  @Column(name = "expense", nullable = false, precision = 12, scale = 2)
  private BigDecimal expense;

  @ColumnDefault("0")
  @Column(name = "active_members", nullable = false)
  private Short activeMembers;

  @ColumnDefault("0")
  @Column(name = "new_members", nullable = false)
  private Short newMembers;

  @ColumnDefault("0")
  @Column(name = "churned_members", nullable = false)
  private Short churnedMembers;

  @Column(name = "churn_rate", precision = 5, scale = 2)
  private BigDecimal churnRate;

  @Column(name = "peak_occupancy_pct", precision = 5, scale = 2)
  private BigDecimal peakOccupancyPct;

  @ColumnDefault("0")
  @Column(name = "overdue_amount", nullable = false, precision = 5, scale = 2)
  private BigDecimal overdueAmount;

  @ColumnDefault("false")
  @Column(name = "is_closed", nullable = false)
  private Boolean isClosed;

  @ColumnDefault("now()")
  @Column(name = "calculated_at", nullable = false)
  private LocalDateTime calculatedAt;

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    GymMetricsMO that = (GymMetricsMO) o;
    return getGymBranch() != null && Objects.equals(getGymBranch(), that.getGymBranch())
      && getYearMonth() != null && Objects.equals(getYearMonth(), that.getYearMonth());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(gymBranch, yearMonth);
  }

  @Getter
  @Setter
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer gymBranch;

    private LocalDate yearMonth;

  }

}
