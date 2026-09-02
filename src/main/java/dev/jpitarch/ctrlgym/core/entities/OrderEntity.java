package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrderEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private UserEntity member;

  @Column(name = "gym_id", nullable = false)
  private Integer gymId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gym_branch_id")
  private GymBranchEntity gymBranch;

  @Column(name = "series", nullable = false, length = 20)
  private String series;

  @Column(name = "number", nullable = false, length = 20)
  private String number;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "verifactu_id")
  private UUID verifactuId;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItemEntity> items = new ArrayList<>();
}
