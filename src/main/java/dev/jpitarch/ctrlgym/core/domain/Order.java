package dev.jpitarch.ctrlgym.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  private Integer id;

  @JsonProperty("member_id")
  private UUID memberId;

  @JsonProperty("gym_id")
  private Integer gymId;

  @JsonProperty("gym_branch_id")
  private Integer gymBranchId;

  private String series;

  private String number;

  @JsonProperty("created_at")
  private OffsetDateTime createdAt;

  @JsonProperty("verifactu_id")
  private UUID verifactuId;

  private List<Item> items;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Item {

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("product_price")
    private BigDecimal productPrice;

    private Integer quantity;
  }
}
