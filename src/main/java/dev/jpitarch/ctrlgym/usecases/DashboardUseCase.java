package dev.jpitarch.ctrlgym.usecases;

import dev.jpitarch.ctrlgym.domain.DatePeriod;
import dev.jpitarch.ctrlgym.domain.GymBranchId;
import dev.jpitarch.ctrlgym.domain.enums.Granularity;
import dev.jpitarch.ctrlgym.domain.enums.MembershipFlow;
import dev.jpitarch.ctrlgym.repositories.ExpensesRepository;
import dev.jpitarch.ctrlgym.repositories.GymBranchOccupancyRepository;
import dev.jpitarch.ctrlgym.repositories.MembershipsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardUseCase {

  private final GymBranchOccupancyRepository gymBranchOccupancyRepository;

  private final MembershipsRepository membershipsRepository;

  private final ExpensesRepository expensesRepository;

  public List<String[]> getOccupancies(GymBranchId gymBranchId, DatePeriod datePeriod, Granularity granularity) {
    return gymBranchOccupancyRepository.getOccupancies(gymBranchId, datePeriod, granularity);
  }

  public List<String[]> getMemberships(GymBranchId gymBranchId, DatePeriod datePeriod, MembershipFlow flow) {
    return switch (flow) {
      case ACTIVE -> membershipsRepository.getCurrentCount(gymBranchId, datePeriod);
      case NEW -> membershipsRepository.getNewsCount(gymBranchId, datePeriod);
      case CANCELLED -> membershipsRepository.getCancelledCount(gymBranchId, datePeriod);
    };
  }

  public Integer getMembershipSeniorityAverage(GymBranchId gymBranchId, DatePeriod datePeriod) {
    return membershipsRepository.getSeniorityAverage(gymBranchId, datePeriod);
  }

  public List<String[]> getCohorts(GymBranchId gymBranchId) {
    return membershipsRepository.getCohorts(gymBranchId);
  }

  public List<String[]> getCancellationReasons(GymBranchId gymBranchId) {
    return membershipsRepository.getCancellationReasons(gymBranchId, null);
  }

  public List<String[]> getExpenses(GymBranchId gymBranchId) {
    return expensesRepository.getExpenses(gymBranchId);
  }

}
