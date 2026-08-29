package dev.jpitarch.ctrlgym.core.repositories.jpa;

import dev.jpitarch.ctrlgym.core.entities.ShiftEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftJpaRepository extends JpaRepository<ShiftEntity, Long> {
  List<ShiftEntity> findBySeriesId(Long seriesId);

  List<ShiftEntity> findByEmployeeIdAndGymIdAndGymBranchIdAndShiftDateBetween(UUID employeeId, Integer gymId, Integer gymBranchId, LocalDate from, LocalDate to);

  List<ShiftEntity> findByEmployeeIdAndGymIdAndGymBranchId(UUID employeeId, Integer gymId, Integer gymBranchId);

  void deleteBySeriesId(Long seriesId);
}
