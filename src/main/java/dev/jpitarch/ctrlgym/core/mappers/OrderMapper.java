package dev.jpitarch.ctrlgym.core.mappers;

import dev.jpitarch.ctrlgym.core.domain.Order;
import dev.jpitarch.ctrlgym.core.entities.OrderEntity;
import dev.jpitarch.ctrlgym.core.entities.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface OrderMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "member", expression = "java(order.getMemberId() != null ? new dev.jpitarch.ctrlgym.core.entities.UserEntity() {{ setId(order.getMemberId()); }} : null)")
  @Mapping(target = "gymBranch", expression = "java(order.getGymBranchId() != null ? new dev.jpitarch.ctrlgym.core.entities.GymBranchEntity() {{ setId(order.getGymBranchId()); }} : null)")
  @Mapping(target = "items", source = "items")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "verifactuId", ignore = true)
  @Mapping(target = "series", ignore = true)
  @Mapping(target = "number", ignore = true)
  OrderEntity toEntity(Order order);

  @Mapping(target = "memberId", source = "member.id")
  @Mapping(target = "gymId", source = "gymId")
  @Mapping(target = "gymBranchId", source = "gymBranch.id")
  @Mapping(target = "items", source = "items")
  Order toDomain(OrderEntity entity);

  @Mapping(target = "id.productId", source = "productId")
  @Mapping(target = "id.orderId", ignore = true)
  @Mapping(target = "product", expression = "java(new dev.jpitarch.ctrlgym.core.entities.ProductEntity() {{ setId(item.getProductId()); }})")
  @Mapping(target = "order", ignore = true)
  @Mapping(target = "productNameSnapshot", source = "productName")
  @Mapping(target = "productPriceSnapshot", source = "productPrice")
  OrderItemEntity toItemEntity(Order.Item item);

  @Mapping(target = "productId", source = "id.productId")
  @Mapping(target = "productName", source = "productNameSnapshot")
  @Mapping(target = "productPrice", source = "productPriceSnapshot")
  Order.Item toItem(OrderItemEntity entity);
}
