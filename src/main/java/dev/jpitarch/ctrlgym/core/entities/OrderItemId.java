package dev.jpitarch.ctrlgym.core.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class OrderItemId implements Serializable {

  @Column(name = "order_id", nullable = false)
  private Integer orderId;

  @Column(name = "product_id", nullable = false)
  private Integer productId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OrderItemId that = (OrderItemId) o;
    return Objects.equals(orderId, that.orderId) && Objects.equals(productId, that.productId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderId, productId);
  }
}
