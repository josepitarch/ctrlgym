package dev.jpitarch.ctrlgym.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateOrderRequest {

  @JsonProperty("member_id")
  private UUID memberId;

  private List<Item> items;

  @Getter
  @Setter
  public static class Item {

    @JsonProperty("product_id")
    private Integer productId;

    private Integer quantity;
  }
}
