package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.ExpenseCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseCategoryJpaRepository extends JpaRepository<ExpenseCategoryEntity, Integer> {

  List<ExpenseCategoryEntity> findAllByGymId(Integer gymId);

}
