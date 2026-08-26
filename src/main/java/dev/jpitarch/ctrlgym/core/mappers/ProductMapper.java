package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Product;
import dev.jpitarch.ctrlgym.core.models.ProductMO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface ProductMapper {

  Product map(ProductMO mo);

  @Mapping(target = "gymId", ignore = true)
  @Mapping(target = "gymBranch", ignore = true)
  ProductMO map(Product product);

}
