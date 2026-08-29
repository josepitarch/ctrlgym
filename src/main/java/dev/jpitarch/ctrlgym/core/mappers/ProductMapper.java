package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Product;
import dev.jpitarch.ctrlgym.core.entities.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface ProductMapper {

  Product map(ProductEntity mo);

  @Mapping(target = "gymId", ignore = true)
  @Mapping(target = "gymBranch", ignore = true)
  ProductEntity map(Product product);

}
