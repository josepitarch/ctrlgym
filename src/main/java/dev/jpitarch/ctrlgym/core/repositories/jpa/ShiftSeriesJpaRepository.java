package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.ShiftSeriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftSeriesJpaRepository extends JpaRepository<ShiftSeriesEntity, Long> {
  List<ShiftSeriesEntity> findByEmployeeIdAndGymIdAndGymBranchId(UUID employeeId, Integer gymId, Integer gymBranchId);
}
