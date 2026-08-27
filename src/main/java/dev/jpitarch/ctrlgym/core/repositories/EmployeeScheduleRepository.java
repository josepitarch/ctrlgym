package dev.jpitarch.ctrlgym.core.repositories;

import dev.jpitarch.ctrlgym.core.domain.Shift;
import dev.jpitarch.ctrlgym.core.domain.ShiftSeries;
import dev.jpitarch.ctrlgym.core.mappers.ShiftMapper;
import dev.jpitarch.ctrlgym.core.models.ShiftMO;
import dev.jpitarch.ctrlgym.core.models.ShiftSeriesMO;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ShiftJpaRepository;
import dev.jpitarch.ctrlgym.core.repositories.jpa.ShiftSeriesJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EmployeeScheduleRepository {

  private final ShiftSeriesJpaRepository seriesJpaRepository;

  private final ShiftJpaRepository shiftJpaRepository;

  private final ShiftMapper shiftMapper;

  public ShiftSeries saveSeries(ShiftSeries series) {
    ShiftSeriesMO mo = shiftMapper.map(series);
    mo.setCreatedAt(OffsetDateTime.now());
    ShiftSeriesMO saved = seriesJpaRepository.save(mo);
    return shiftMapper.map(saved);
  }

  public Shift saveShift(Shift shift) {
    ShiftMO mo = shiftMapper.map(shift);
    mo.setCreatedAt(OffsetDateTime.now());
    ShiftMO saved = shiftJpaRepository.save(mo);
    return shiftMapper.map(saved);
  }

  public List<Shift> saveAllShifts(List<Shift> shifts) {
    List<ShiftMO> mos = shifts.stream()
      .map(s -> {
        ShiftMO mo = shiftMapper.map(s);
        mo.setCreatedAt(OffsetDateTime.now());
        return mo;
      })
      .toList();
    return shiftJpaRepository.saveAll(mos).stream().map(shiftMapper::map).toList();
  }

  public Optional<ShiftSeries> findSeriesById(Long seriesId) {
    return seriesJpaRepository.findById(seriesId).map(shiftMapper::map);
  }

  public Optional<Shift> findShiftById(Long shiftId) {
    return shiftJpaRepository.findById(shiftId).map(shiftMapper::map);
  }

  public List<Shift> findShiftsBySeriesId(Long seriesId) {
    return shiftJpaRepository.findBySeriesId(seriesId).stream().map(shiftMapper::map).toList();
  }

  public List<Shift> findShiftsByEmployeeAndDateRange(UUID employeeId, Integer gymId, Integer gymBranchId, LocalDate from, LocalDate to) {
    return shiftJpaRepository
      .findByEmployeeIdAndGymIdAndGymBranchIdAndShiftDateBetween(employeeId, gymId, gymBranchId, from, to)
      .stream()
      .map(shiftMapper::map)
      .toList();
  }

  public List<Shift> findShiftsByEmployee(UUID employeeId, Integer gymId, Integer gymBranchId) {
    return shiftJpaRepository
      .findByEmployeeIdAndGymIdAndGymBranchId(employeeId, gymId, gymBranchId)
      .stream()
      .map(shiftMapper::map)
      .toList();
  }

  public List<ShiftSeries> findSeriesByEmployee(UUID employeeId, Integer gymId, Integer gymBranchId) {
    return seriesJpaRepository
      .findByEmployeeIdAndGymIdAndGymBranchId(employeeId, gymId, gymBranchId)
      .stream()
      .map(shiftMapper::map)
      .toList();
  }

  @Transactional
  public void deleteSeries(Long seriesId) {
    shiftJpaRepository.deleteBySeriesId(seriesId);
    seriesJpaRepository.deleteById(seriesId);
  }

  public void deleteShift(Long shiftId) {
    shiftJpaRepository.deleteById(shiftId);
  }

}
