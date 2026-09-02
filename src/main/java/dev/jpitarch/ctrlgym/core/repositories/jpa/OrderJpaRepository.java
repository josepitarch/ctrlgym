package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, Integer> {

  List<OrderEntity> findByGymBranchIdOrderByCreatedAtDesc(Integer gymBranchId);

  Optional<OrderEntity> findById(Integer id);
}
