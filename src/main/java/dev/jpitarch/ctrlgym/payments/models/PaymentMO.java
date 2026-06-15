package dev.jpitarch.ctrlgym.payments.models;

import dev.jpitarch.ctrlgym.core.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class PaymentMO {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "invoice_id", nullable = false)
  private String invoiceId;

  @Column(name = "stripe_payment_intent_id", length = 100)
  private String stripePaymentIntentId;

  @Column(name = "amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(name = "method", nullable = false, length = 20)
  private String method;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false, length = 20)
  private PaymentStatus status;

  @Column(name = "paid_at")
  private Instant paidAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    PaymentMO paymentMO = (PaymentMO) o;
    return getId() != null && Objects.equals(getId(), paymentMO.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
  }
}
