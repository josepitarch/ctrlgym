package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.models.ShiftMO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftJpaRepository extends JpaRepository<ShiftMO, Long> {
  List<ShiftMO> findBySeriesId(Long seriesId);

  List<ShiftMO> findByEmployeeIdAndGymIdAndGymBranchIdAndShiftDateBetween(UUID employeeId, Integer gymId, Integer gymBranchId, LocalDate from, LocalDate to);

  List<ShiftMO> findByEmployeeIdAndGymIdAndGymBranchId(UUID employeeId, Integer gymId, Integer gymBranchId);

  void deleteBySeriesId(Long seriesId);
}
