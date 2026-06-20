package dev.jpitarch.ctrlgym.payments.models;

import dev.jpitarch.ctrlgym.core.domain.Member;
import dev.jpitarch.ctrlgym.core.domain.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "invoices")
public class InvoiceMO {

  @Id
  @Column(name = "id")
  private String id;

  @Getter(AccessLevel.NONE)
  @Column(name = "gym_id", nullable = false)
  private Integer gymId;

  @Getter(AccessLevel.NONE)
  @Column(name = "member_id", nullable = false)
  private UUID memberId;

  @Column(name = "series", nullable = false, length = 20)
  private String series;

  @Column(name = "number", nullable = false, length = 20)
  private String number;

  @Column(name = "issue_at", nullable = false)
  private LocalDate issueAt;

  @Column(name = "due_at", nullable = false)
  private LocalDate dueAt;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false)
  private InvoiceStatus status;

  @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
  private BigDecimal subtotal;

  @ColumnDefault("0")
  @Column(name = "tax", nullable = false, precision = 10, scale = 2)
  private BigDecimal tax;

  @Column(name = "total", nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @Column(name = "verifactu_id")
  private UUID verifactuId;

  @Column(name = "stripe_invoice_number", nullable = false, length = 50)
  private String stripeInvoiceNumber;

  public Member.Id getMemberId() {
    return Member.Id.of(memberId, gymId);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    InvoiceMO invoiceMO = (InvoiceMO) o;
    return getId() != null && Objects.equals(getId(), invoiceMO.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
  }
}
