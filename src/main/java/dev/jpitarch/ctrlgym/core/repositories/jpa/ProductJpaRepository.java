package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.ProductMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductMO, Integer> {

  List<ProductMO> findByGymBranchId(Integer gymBranchId);

}
