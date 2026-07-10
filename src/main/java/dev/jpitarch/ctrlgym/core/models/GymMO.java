package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "gyms")
public class GymMO {
  @Id
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "verifacti_api_key", nullable = false, length = Integer.MAX_VALUE)
  private String verifactiApiKey;

  @Column(name = "stripe_account_id", length = Integer.MAX_VALUE)
  private String stripeAccountId;

  @OneToMany(mappedBy = "gym", fetch = FetchType.LAZY)
  List<GymBranchMO> branches;


}
