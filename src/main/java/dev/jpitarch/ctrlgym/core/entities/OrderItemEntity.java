package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItemEntity {

  @EmbeddedId
  private OrderItemId id = new OrderItemId();

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("orderId")
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("productId")
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  @Column(name = "product_name_snapshot", nullable = false)
  private String productNameSnapshot;

  @Column(name = "product_price_snapshot", nullable = false, precision = 4, scale = 2)
  private BigDecimal productPriceSnapshot;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;
}
