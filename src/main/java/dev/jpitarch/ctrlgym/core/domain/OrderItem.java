package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

  @JsonProperty("product_id")
  private Integer productId;

  @JsonProperty("product_name_snapshot")
  private String productNameSnapshot;

  @JsonProperty("product_price_snapshot")
  private BigDecimal productPriceSnapshot;

  private Integer quantity;
}
