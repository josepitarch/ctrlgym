package dev.jpitarch.ctrlgym.core.models;

import dev.jpitarch.ctrlgym.core.domain.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "members")
@IdClass(MemberMO.ID.class)
public class MemberMO {

  @Id
  @Column(name = "id")
  private UUID id;

  @Id
  @Column(name = "gym_id")
  private Integer gymId;

  @Column(name = "email", length = Integer.MAX_VALUE)
  private String email;

  @Column(name = "nif")
  private String nif;

  @Column(name = "name", nullable = false, length = 20)
  private String name;

  @Column(name = "first_surname", length = 20)
  private String firstSurname;

  @Column(name = "second_surname", length = 20)
  private String secondSurname;

  @Column(name = "avatar_url", length = Integer.MAX_VALUE)
  private String avatarUrl;

  @Column(name = "gender", nullable = false, length = Integer.MAX_VALUE)
  private String gender;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "street")
  private String street;

  @Column(name = "city")
  private String city;

  @Column(name = "state")
  private String state;

  @Column(name = "country")
  private String country;

  @Column(name = "postal_code", length = 20)
  private Integer postalCode;

  @ColumnDefault("now()")
  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false)
  private MemberStatus status;

  @Column(name = "stripe_customer_id", length = Integer.MAX_VALUE)
  private String stripeCustomerId;

  @Column(name = "stripe_payment_method_id", length = Integer.MAX_VALUE)
  private String stripePaymentMethodId;


  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    MemberMO memberMO = (MemberMO) o;
    return getId() != null && Objects.equals(getId(), memberMO.getId())
            && getGymId() != null && Objects.equals(getGymId(), memberMO.getGymId());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(id, gymId);
  }

  @Getter
  @Setter
  @EqualsAndHashCode
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = -1302489486342450890L;

    private UUID id;

    private Integer gymId;

  }

}
