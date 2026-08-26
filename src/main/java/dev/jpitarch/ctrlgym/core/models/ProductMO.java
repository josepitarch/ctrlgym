package dev.jpitarch.ctrlgym.core.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "products")
public class ProductMO {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "gym_id", nullable = false)
  private Integer gymId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "gym_branch_id", nullable = false)
  private GymBranchMO gymBranch;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "image")
  private String image;

  @Column(name = "price", nullable = false, precision = 4, scale = 2)
  private BigDecimal price;

  @Column(name = "stock")
  private Short stock;

}
