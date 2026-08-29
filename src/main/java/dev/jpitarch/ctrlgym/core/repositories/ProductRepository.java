package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Product;
import dev.jpitarch.ctrlgym.core.mappers.ProductMapper;
import dev.jpitarch.ctrlgym.core.entities.GymBranchEntity;
import dev.jpitarch.ctrlgym.core.entities.ProductEntity;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

  private final ProductJpaRepository jpaRepository;

  private final ProductMapper mapper;

  public List<Product> findByBranchId(Integer branchId) {
    return jpaRepository.findByGymBranchId(branchId)
      .stream()
      .map(mapper::map)
      .toList();
  }

  public Product create(Product product, Integer gymId, Integer branchId) {
    ProductEntity mo = mapper.map(product);
    mo.setGymId(gymId);
    GymBranchEntity branch = new GymBranchEntity();
    branch.setId(branchId);
    mo.setGymBranch(branch);
    ProductEntity saved = jpaRepository.save(mo);
    return mapper.map(saved);
  }

  public Optional<Product> findById(Integer productId) {
    return jpaRepository.findById(productId)
      .map(mapper::map);
  }

  public void delete(Integer productId) {
    jpaRepository.deleteById(productId);
  }

}
