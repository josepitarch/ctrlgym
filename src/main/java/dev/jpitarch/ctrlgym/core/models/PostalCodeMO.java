package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@IdClass(PostalCodeMO.ID.class)
@Table(name = "postal_codes")
public class PostalCodeMO {

  @Id
  @Column(name = "postal_code", nullable = false, length = 5)
  private String postalCode;

  @Id
  @Column(name = "city", nullable = false, length = 150)
  private String city;

  @Column(name = "province", nullable = false, length = 100)
  private String province;

  @Column(name = "state", length = 100)
  private String state;


  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    PostalCodeMO that = (PostalCodeMO) o;
    return getPostalCode() != null && Objects.equals(getPostalCode(), that.getPostalCode())
      && getCity() != null && Objects.equals(getCity(), that.getCity());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(postalCode, city);
  }

  @Getter
  @Setter
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = -2592927008260883790L;

    private String postalCode;

    private String city;

  }
}
