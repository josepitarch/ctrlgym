package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Entity
@IdClass(MembershipCancellationReasonTranslationMO.ID.class)
@Table(name = "membership_cancellation_reason_translations")
public class MembershipCancellationReasonTranslationMO {

  @Id
  @Column(name = "cancellation_reason_id", nullable = false)
  private Integer cancellationReasonId;

  @Id
  @Column(name = "language_code", nullable = false, length = 10)
  private String languageCode;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cancellation_reason_id", nullable = false)
  private MembershipCancellationReasonMO cancellationReason;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", length = Integer.MAX_VALUE)
  private String description;


  @Getter
  @Setter
  @Embeddable
  @EqualsAndHashCode
  public static class ID implements Serializable {

    @Serial
    private static final long serialVersionUID = -3072837731330150469L;

    private Integer cancellationReasonId;

    private String languageCode;

  }

}
