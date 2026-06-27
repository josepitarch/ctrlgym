package dev.jpitarch.ctrlgym.payments.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class MemberId implements Serializable {
  private static final long serialVersionUID = -6555687170435618849L;
  @Column(name = "memberId", nullable = false)
  private UUID id;

  @Column(name = "gym_id", nullable = false)
  private Integer gymId;


}
