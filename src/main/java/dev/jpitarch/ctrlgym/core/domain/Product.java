package dev.jpitarch.ctrlgym.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

  private Integer id;

  private String name;

  private String image;

  private BigDecimal price;

  private Short stock;

}
