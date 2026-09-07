package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "member_metrics_monthly")
@IdClass(MemberMetricsEntity.ID.class)
public class MemberMetricsEntity {

  @Id
  @Column(name = "member_id")
  private UUID member;

  @Id
  @Column(name = "year_month", nullable = false)
  private LocalDate yearMonth;

  @ColumnDefault("0")
  @Column(name = "attendance", nullable = false)
  private Short attendance;

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
    MemberMetricsEntity that = (MemberMetricsEntity) o;
    return getMember() != null && Objects.equals(getMember(), that.getMember())
      && getYearMonth() != null && Objects.equals(getYearMonth(), that.getYearMonth());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(member, yearMonth);
  }

  @Getter
  @Setter
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID member;

    private LocalDate yearMonth;

  }

}
