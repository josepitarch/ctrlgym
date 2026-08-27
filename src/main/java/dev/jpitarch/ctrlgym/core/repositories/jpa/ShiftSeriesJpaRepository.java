package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.ShiftSeriesMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftSeriesJpaRepository extends JpaRepository<ShiftSeriesMO, Long> {
  List<ShiftSeriesMO> findByEmployeeIdAndGymIdAndGymBranchId(UUID employeeId, Integer gymId, Integer gymBranchId);
}
