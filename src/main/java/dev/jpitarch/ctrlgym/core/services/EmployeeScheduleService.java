package dev.jpitarch.ctrlgym.core.services;

import dev.jpitarch.ctrlgym.core.domain.Shift;
import dev.jpitarch.ctrlgym.core.domain.ShiftSeries;
import dev.jpitarch.ctrlgym.core.domain.enums.ShiftStatus;
import dev.jpitarch.ctrlgym.core.dto.CreateShiftRequest;
import dev.jpitarch.ctrlgym.core.dto.CreateShiftSeriesRequest;
import dev.jpitarch.ctrlgym.core.dto.UpdateShiftRequest;
import dev.jpitarch.ctrlgym.core.repositories.EmployeeScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeScheduleService {

  private static final int DEFAULT_HORIZON_MONTHS = 3;

  private final EmployeeScheduleRepository repository;

  @Transactional
  public ShiftSeries createSeries(Integer gymId, Integer gymBranchId, CreateShiftSeriesRequest request) {
    ShiftSeries series = new ShiftSeries();
    series.setEmployeeId(request.employeeId());
    series.setGymId(gymId);
    series.setGymBranchId(gymBranchId);
    series.setStartTime(request.startTime());
    series.setEndTime(request.endTime());
    series.setRecurrenceType(request.recurrenceType());
    series.setIntervalValue(request.intervalValue() != null ? request.intervalValue() : 1);
    series.setDaysOfWeek(request.daysOfWeek());
    series.setSeriesStart(request.seriesStart());
    series.setSeriesEnd(request.seriesEnd());

    ShiftSeries saved = repository.saveSeries(series);

    List<Shift> shifts = generateOccurrences(saved);
    repository.saveAllShifts(shifts);

    return saved;
  }

  @Transactional
  public Shift createSingleShift(Integer gymId, Integer gymBranchId, CreateShiftRequest request) {
    Shift shift = new Shift();
    shift.setSeriesId(null);
    shift.setEmployeeId(request.employeeId());
    shift.setGymId(gymId);
    shift.setGymBranchId(gymBranchId);
    shift.setShiftDate(request.shiftDate());
    shift.setStartTime(request.startTime());
    shift.setEndTime(request.endTime());
    shift.setStatus(ShiftStatus.SCHEDULED);
    shift.setException(false);

    return repository.saveShift(shift);
  }

  public List<Shift> getShiftsByDateRange(UUID employeeId, Integer gymId, Integer gymBranchId, LocalDate from, LocalDate to) {
    return repository.findShiftsByEmployeeAndDateRange(employeeId, gymId, gymBranchId, from, to);
  }

  public List<Shift> getAllShifts(UUID employeeId, Integer gymId, Integer gymBranchId) {
    return repository.findShiftsByEmployee(employeeId, gymId, gymBranchId);
  }

  public List<ShiftSeries> getAllSeries(UUID employeeId, Integer gymId, Integer gymBranchId) {
    return repository.findSeriesByEmployee(employeeId, gymId, gymBranchId);
  }

  @Transactional
  public void deleteSeries(Long seriesId) {
    repository.findSeriesById(seriesId)
      .orElseThrow(() -> new NoSuchElementException("Series not found: " + seriesId));
    repository.deleteSeries(seriesId);
  }

  @Transactional
  public void deleteShift(Long shiftId) {
    Shift shift = repository.findShiftById(shiftId)
      .orElseThrow(() -> new NoSuchElementException("Shift not found: " + shiftId));

    if (shift.getSeriesId() != null) {
      shift.setStatus(ShiftStatus.CANCELLED);
      repository.saveShift(shift);
    } else {
      repository.deleteShift(shiftId);
    }
  }

  @Transactional
  public Shift updateShift(Long shiftId, UpdateShiftRequest request) {
    Shift shift = repository.findShiftById(shiftId)
      .orElseThrow(() -> new NoSuchElementException("Shift not found: " + shiftId));

    if (request.shiftDate() != null) shift.setShiftDate(request.shiftDate());
    if (request.startTime() != null) shift.setStartTime(request.startTime());
    if (request.endTime() != null) shift.setEndTime(request.endTime());

    if (shift.getSeriesId() != null) {
      shift.setException(true);
      shift.setStatus(ShiftStatus.MODIFIED);
    }

    return repository.saveShift(shift);
  }

  private List<Shift> generateOccurrences(ShiftSeries series) {
    LocalDate end = series.getSeriesEnd();
    if (end == null) {
      end = series.getSeriesStart().plusMonths(DEFAULT_HORIZON_MONTHS);
    }

    List<LocalDate> dates = switch (series.getRecurrenceType()) {
      case NONE -> List.of(series.getSeriesStart());
      case DAILY -> generateDailyDates(series.getSeriesStart(), end, series.getIntervalValue());
      case WEEKLY ->
        generateWeeklyDates(series.getSeriesStart(), end, series.getIntervalValue(), series.getDaysOfWeek());
      case MONTHLY -> generateMonthlyDates(series.getSeriesStart(), end, series.getIntervalValue());
    };

    return dates.stream()
      .map(date -> {
        Shift shift = new Shift();
        shift.setSeriesId(series.getId());
        shift.setEmployeeId(series.getEmployeeId());
        shift.setGymId(series.getGymId());
        shift.setGymBranchId(series.getGymBranchId());
        shift.setShiftDate(date);
        shift.setStartTime(series.getStartTime());
        shift.setEndTime(series.getEndTime());
        shift.setStatus(ShiftStatus.SCHEDULED);
        shift.setException(false);
        return shift;
      })
      .toList();
  }

  private List<LocalDate> generateDailyDates(LocalDate start, LocalDate end, int interval) {
    List<LocalDate> dates = new ArrayList<>();
    LocalDate current = start;
    while (!current.isAfter(end)) {
      dates.add(current);
      current = current.plusDays(interval);
    }
    return dates;
  }

  private List<LocalDate> generateWeeklyDates(LocalDate start, LocalDate end, int interval, List<Short> daysOfWeek) {
    List<LocalDate> dates = new ArrayList<>();
    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      dates.add(start);
      return dates;
    }

    Set<DayOfWeek> targetDays = daysOfWeek.stream()
      .map(d -> DayOfWeek.of(d))
      .collect(java.util.stream.Collectors.toSet());

    LocalDate weekStart = start.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    while (!weekStart.isAfter(end)) {
      for (DayOfWeek day : targetDays) {
        LocalDate candidate = weekStart.with(day);
        if (!candidate.isBefore(start) && !candidate.isAfter(end)) {
          dates.add(candidate);
        }
      }
      weekStart = weekStart.plusWeeks(interval);
    }

    Collections.sort(dates);
    return dates;
  }

  private List<LocalDate> generateMonthlyDates(LocalDate start, LocalDate end, int interval) {
    List<LocalDate> dates = new ArrayList<>();
    int targetDay = start.getDayOfMonth();
    LocalDate current = start;

    while (!current.isAfter(end)) {
      int lastDayOfMonth = current.lengthOfMonth();
      int day = Math.min(targetDay, lastDayOfMonth);
      LocalDate candidate = current.withDayOfMonth(day);
      if (!candidate.isBefore(start) && !candidate.isAfter(end)) {
        dates.add(candidate);
      }
      current = current.plusMonths(interval);
    }
    return dates;
  }
}
