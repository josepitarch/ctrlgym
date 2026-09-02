package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Order;
import dev.jpitarch.ctrlgym.core.domain.exceptions.OrderNotFoundException;
import dev.jpitarch.ctrlgym.core.entities.OrderEntity;
import dev.jpitarch.ctrlgym.core.entities.OrderItemEntity;
import dev.jpitarch.ctrlgym.core.mappers.OrderMapper;
import dev.jpitarch.ctrlgym.core.repositories.jpa.OrderJpaRepository;
import dev.jpitarch.ctrlgym.core.components.InvoiceCounterComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderRepository {

  private final OrderJpaRepository jpaRepository;
  private final OrderMapper mapper;
  private final InvoiceCounterComponent invoiceCounterComponent;

  public Order create(Order order) {
    OrderEntity entity = mapper.toEntity(order);
    entity.setCreatedAt(OffsetDateTime.now());

    String series = order.getGymId() + "-" + Year.now();
    Integer number = invoiceCounterComponent.nextNumber(order.getGymId(), series);
    entity.setSeries(series);
    entity.setNumber(number.toString());

    List<OrderItemEntity> items = new ArrayList<>(entity.getItems());
    entity.getItems().clear();
    OrderEntity saved = jpaRepository.save(entity);
    items.forEach(item -> {
      item.setOrder(saved);
      item.getId().setOrderId(saved.getId());
    });
    saved.getItems().addAll(items);
    OrderEntity savedWithItems = jpaRepository.save(saved);
    return mapper.toDomain(savedWithItems);
  }

  public List<Order> findByBranchId(Integer gymBranchId) {
    return jpaRepository.findByGymBranchIdOrderByCreatedAtDesc(gymBranchId)
      .stream()
      .map(mapper::toDomain)
      .toList();
  }

  public Optional<Order> findById(Integer id) {
    return jpaRepository.findById(id)
      .map(mapper::toDomain);
  }

  public void saveVerifactuId(Integer id, UUID verifactuId) {
    var orderEntity = jpaRepository
      .findById(id)
      .orElseThrow(() -> new OrderNotFoundException(id));

    log.info("Saving verifactuId to order with id {}: {}...", id, verifactuId);

    orderEntity.setVerifactuId(verifactuId);
    jpaRepository.save(orderEntity);
  }
}
